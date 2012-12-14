package org.jas.datafile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nanoxml.XMLElement;

import org.jas.commun.JasException;
import org.jas.zip.ZipHelper;

/**
 * Classe permettant de lire un fichier DAT.
 */
public final class Datafile implements UpdateListener {
   /**
    * Nom de l'élément XML game.
    */
   private static final String GAME_XML_ELEMENT = "game";

   /**
    * Nom de l'élément XML datVersion.
    */
   private static final String DATVERSION_XML_ELEMENT = "datVersion";

   /**
    * Nom de l'élément XML system.
    */
   private static final String SYSTEM_XML_ELEMENT = "system";

   /**
    * Temps de pause lors du téléchargement du fichier DAT.
    */
   private static final long SLEEP_TIME = 500;

   /**
    * Constante pour la base 16 (hexadécimale).
    */
   private static final int HEXADECIMAL_RADIX = 16;

   /**
    * Chemin vers le fichier DAT.
    */
   private String datafilePath = null;

   /**
    * Version du DAT.
    */
   private String datVersion = null;

   /**
    * Nom du système.
    */
   private String system = null;

   /**
    * Url du fichier contenant le numéro de version le plus récent.
    */
   private String datVersionURL = null;

   /**
    * Url du fichier DAT.
    */
   private String datURL = null;

   /**
    * Format de nommage des ROMs.
    */
   private String romTitle = null;

   /**
    * Liste des extensions connues pour les ROMs.
    */
   private List<String> listKnownExtensions = null;

   /**
    * Map des jeux (clé = CRC32).
    */
   private Map<Long, Game> mapGames = new HashMap<Long, Game>();

   /**
    * Liste des jeux possédés.
    */
   private List<Game> listGamesHave = null;

   /**
    * Liste des jeux non possédés.
    */
   private List<Game> listGamesMiss = null;

   /**
    * Map des jeux en double.
    */
   private Map<Integer, List<Game>> mapGamesDupe = null;

   /**
    * Indique qu'un nouveau fichier DAT est disponible.
    */
   private boolean newDatAvailable = false;

   /**
    * Indique si la mise à jour éventuelle d'un fichier DAT a été faite.
    */
   private boolean updateChecked = false;

   /**
    * Constructeur par défault.
    *
    * @param aDatafilePath
    *           Le chemin du fichier DAT.
    */
   public Datafile(final String aDatafilePath) {
      super();
      this.datafilePath = aDatafilePath;
   }

   /**
    * Parser le fichier DAT.
    *
    * @throws JasException
    *            L'exception levée lors du parcours du fichier XML.
    */
   public void parseDatafile() throws JasException {
      if (datafilePath != null) {
         // On s'abonne aux notifications de l'UpdateListener.
         UpdateNotifier.getInstance().addUpdateListener(this);

         // On parse le fichier DAT.
         parse();

         // On vérifie si le parsing XML a été stoppé.
         if (newDatAvailable || !updateChecked) {
            while (!updateChecked) {
               try {
                  Thread.sleep(SLEEP_TIME);
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }
            }

            if (newDatAvailable) {
               // On relance le parsing avec la nouvelle version.
               parse();
            }
         }
      }
   }

   /**
    * Lancer le parsing du fichier DAT.
    *
    * @throws JasException
    *            L'exception levée lors du parcours du fichier XML.
    */
   private void parse() throws JasException {
      // On n'a pas encore vérifié le nouveau DAT ou bien c'est déjà fait.
      newDatAvailable = false;
      // On regarde si le fichier datafile est sous forme de ZIP.
      boolean zipped = ZipHelper.isZipFile(datafilePath);
      String xmlFileName = null;

      if (zipped) {
         xmlFileName = (String) ZipHelper.unzip(datafilePath).get(0);
      } else {
         xmlFileName = datafilePath;
      }

      System.out.println("Analyse du fichier XML " + xmlFileName + " ...");

      // Initialisation de la map des jeux.
      mapGames = new HashMap<Long, Game>();

      // Lecture du fichier XML.
      parseXml(xmlFileName);

      if (zipped) {
         // Suppression du fichier dézippé.
         new File(xmlFileName).delete();
      }

      // On a fini le parsing, on initialise les listes des jeux.
      listGamesMiss = new ArrayList<Game>(mapGames.values());
      listGamesHave = new ArrayList<Game>();
      mapGamesDupe = new HashMap<Integer, List<Game>>();
   }

   /**
    * Parse le fichier XML.
    *
    * @param filename
    *           le chemin du fichier XML.
    * @throws JasException
    *            L'exception levée lors du parcours du fichier XML.
    */
   private void parseXml(final String filename) throws JasException {
      try {
         // Ouverture du fichier XML.
         final FileReader fileReader = new FileReader(filename);
         final BufferedReader bufferedReader = new BufferedReader(fileReader);

         // Parsing du fichier XML.
         final XMLElement xmlElement = new XMLElement();
         xmlElement.parseFromReader(bufferedReader);

         // Fermeture du fichier XML.
         bufferedReader.close();
         fileReader.close();

         // Parcours de l'élément XML.
         parseXmlElement(xmlElement);
      } catch (FileNotFoundException e) {
         throw new JasException(e);
      } catch (IOException e) {
         throw new JasException(e);
      }
   }

   /**
    * Parser un élément XML.
    *
    * @param xmlElement
    *           L'élément XML à parser.
    * @throws JasException
    *            L'exception levée lors du parcours du fichier XML.
    */
   private void parseXmlElement(final XMLElement xmlElement)
         throws JasException {
      // On parcourt les éléments fils.
      Enumeration childrens = xmlElement.enumerateChildren();
      if (childrens != null) {
         while (childrens.hasMoreElements() && !newDatAvailable) {
            final XMLElement child = (XMLElement) childrens.nextElement();

            // Si c'est un jeu.
            if (child.getName().equals(GAME_XML_ELEMENT)) {
               translateGame(child);
            } else {
               // Traduction de l'élément.
               translateConfiguration(child);
            }

            // Parcours récursif.
            parseXmlElement(child);
         }
      }
   }

   /**
    * Permet de traduire un élément de configuration.
    *
    * @param xmlElement
    *           L'élément XML de configuration.
    * @throws JasException
    *            L'exception levée lors de la traduction de l'élément.
    */
   private void translateConfiguration(final XMLElement xmlElement)
         throws JasException {
      // Récupération du nom de l'élément XML.
      final String name = xmlElement.getName();

      if (name.equals(DATVERSION_XML_ELEMENT)) {
         datVersion = xmlElement.getContent();
         updateDat();
      } else if (name.equals(SYSTEM_XML_ELEMENT)) {
         system = xmlElement.getContent();
      } else if (name.equals("datVersionURL")) {
         datVersionURL = xmlElement.getContent();
         updateDat();
      } else if (name.equals("datURL")) {
         datURL = xmlElement.getContent();
         updateDat();
      } else if (name.equals("romTitle")) {
         romTitle = xmlElement.getContent();
      } else if (name.equals("canOpen")) {
         final Enumeration canOpenChildren = xmlElement.enumerateChildren();
         listKnownExtensions = new ArrayList<String>();
         while (canOpenChildren.hasMoreElements()) {
            XMLElement canOpenChild = (XMLElement) canOpenChildren
                  .nextElement();
            if (canOpenChild.getName().equals("extension")) {
               listKnownExtensions.add(canOpenChild.getContent());
            }
         }
      }
   }

   /**
    * Permet de traduire un élément de jeu.
    *
    * @param xmlElement
    *           L'élément XML de jeu.
    */
   private void translateGame(final XMLElement xmlElement) {
      final Enumeration gameInfos = xmlElement.enumerateChildren();
      final Game game = new Game();

      while (gameInfos.hasMoreElements()) {
         final XMLElement gameInfo = (XMLElement) gameInfos.nextElement();

         final String name = gameInfo.getName();

         if (name.equals("releaseNumber")) {
            game.setReleaseNumber(parseXmlContentToInt(gameInfo.getContent()));
         } else if (name.equals("title")) {
            game.setTitle(gameInfo.getContent());
         } else if (name.equals("location")) {
            game.setLocation(parseXmlContentToLong(gameInfo.getContent()));
         } else if (name.equals("language")) {
            game.setLanguage(parseXmlContentToLong(gameInfo.getContent()));
         } else if (name.equals("comment")) {
            game.setComment(gameInfo.getContent());
         } else if (name.equals("files")) {
            Enumeration filesChildren = gameInfo.enumerateChildren();
            while (filesChildren.hasMoreElements()) {
               final XMLElement filesChild = (XMLElement) filesChildren
                     .nextElement();
               if (filesChild.getName().equals("romCRC")) {
                  game.setExtension(filesChild.getStringAttribute("extension"));
                  game.setRomCRC(parseXmlContentHexToLong(filesChild
                        .getContent()));
               }
            }
         } else if (name.equals("duplicateID")) {
            game.setDuplicateID(parseXmlContentToInt(gameInfo.getContent()));
         }
      }

      // On affecte le format de nommage de la ROM au jeu.
      game.setRomTitle(romTitle);

      // On ajoute le jeu à la Map.
      mapGames.put(new Long(game.getRomCRC()), game);
   }

   /**
    * Permet de parser du contenu XML sous forme de int.
    *
    * @param content
    *           Le contenu à parser.
    * @return La valeur du contenu sous forme de int.
    */
   private int parseXmlContentToInt(final String content) {
      return Integer.parseInt(content);
   }

   /**
    * Permet de parser du contenu XML sous forme de long.
    *
    * @param content
    *           Le contenu à parser.
    * @return La valeur du contenu sous forme de long.
    */
   private long parseXmlContentToLong(final String content) {
      return Long.parseLong(content);
   }

   /**
    * Permet de parser du contenu XML sous forme hexadécimal.
    *
    * @param content
    *           Le contenu à parser.
    * @return La valeur du contenu en base décimale.
    */
   private long parseXmlContentHexToLong(final String content) {
      return Long.parseLong(content, HEXADECIMAL_RADIX);
   }

   /**
    * Renvoie un jeu en fonction de son crc32.
    *
    * @param crc32
    *           Le crc32 du fichier.
    * @return Le jeu correspondant au crc32 ou null si le crc32 est inconnu.
    */
   public Game getGame(final long crc32) {
      return (Game) mapGames.get(Long.valueOf(crc32));
   }

   /**
    * Permet de vérifier si une nouvelle version du DAT est disponible.
    *
    * @throws JasException
    *            L'exception levée lors de la vérification du nouveau DAT.
    */
   private void updateDat() throws JasException {
      // On vérifie que tous les paramètres nécessaires sont bien renseignés.
      if (datVersion != null && datVersionURL != null && datURL != null
            && !updateChecked) {
         UpdateNotifier.getInstance().checkUpdate(datVersion, datVersionURL,
               datURL, datafilePath);
      }
   }

   /**
    * Le téléchargement du nouveau fichier DAT est terminé.
    */
   public void downloadFinished() {
      // On peut relancer le parsing XML.
      updateChecked = true;
   }

   /**
    * Une nouvelle version du fichier DAT est disponible.
    */
   public void newVersionAvailable() {
      // On stoppe le parsing XML.
      newDatAvailable = true;
   }

   /**
    * Indique qu'aucune nouvelle version du DAT n'est disponible.
    */
   public void noNewVersionAvailable() {
      // L'update éventuel a été vérifié.
      updateChecked = true;
   }

   /**
    * Renvoie le système du DAT.
    *
    * @return Le système du DAT.
    */
   public String getSystem() {
      return system;
   }

   /**
    * Indique qu'un jeu est possédé.
    *
    * @param game
    *           Le jeu possédé.
    */
   public void addHaveGame(final Game game) {
      if (listGamesMiss.contains(game)) {
         listGamesMiss.remove(game);
         listGamesHave.add(game);
      }

      // Si ce jeu a des doublons.
      if (game.getDuplicateID() > 0) {
         // On récupère la liste des jeux par duplicateID.
         List<Game> listGamesDupe = mapGamesDupe.get(new Integer(game
               .getDuplicateID()));

         // Si on ne l'a pas encore.
         if (listGamesDupe == null) {
            listGamesDupe = new ArrayList<Game>();
            mapGamesDupe.put(new Integer(game.getDuplicateID()), listGamesDupe);
         }

         listGamesDupe.add(game);
      }
   }

   /**
    * Renvoie le nombre total de jeux du DAT.
    *
    * @return Le nombre total de jeux du DAT.
    */
   public int getTotalGames() {
      int result = 0;

      if (mapGames != null) {
         result = mapGames.values().size();
      }

      return result;
   }

   /**
    * Renvoie la liste des jeux possédés.
    *
    * @return La liste des jeux possédés.
    */
   public List<Game> getListGamesHave() {
      return listGamesHave;
   }

   /**
    * Renvoie la liste des jeux non possédés.
    *
    * @return La liste des jeux non possédés.
    */
   public List<Game> getListGamesMiss() {
      return listGamesMiss;
   }

   /**
    * Renvoie la liste des jeux en double.
    *
    * @return La liste des jeux en double.
    */
   public Map<Integer, List<Game>> getMapGamesDupes() {
      return mapGamesDupe;
   }

   /**
    * Renvoie la liste des extensions connues.
    *
    * @return La liste des extensions connues.
    */
   public List<String> getListKnownExtensions() {
      return listKnownExtensions;
   }

   /**
    * Renvoie le nombre de doublons.
    *
    * @return Le nombre de doublons.
    */
   public int getDupesCount() {
      int dupesCount = 0;
      // Parcours des dupes.

      // Comptage du nombre de dupes.
      for (List<Game> listGamesDupes : mapGamesDupe.values()) {
         if (listGamesDupes.size() > 1) {
            dupesCount += listGamesDupes.size();
         }
      }

      return dupesCount;
   }
}
