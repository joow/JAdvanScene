package org.jas.scanner;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;

import org.jas.commun.Configuration;
import org.jas.commun.Constants;
import org.jas.commun.JasException;
import org.jas.datafile.Datafile;
import org.jas.datafile.Game;
import org.jas.zip.ZipHelper;
import org.jas.zip.ZipQueue;

/**
 * Classe permettant de lancer le scan d'un système donné.
 */
public final class Scanner {
   /**
    * Instance unique de la classe.
    */
   private static final Scanner INSTANCE = new Scanner();

   /**
    * Temps de pause pour attendre la fin du thread de compression.
    */
   private static final long SLEEP_TIME = 500;

   /**
    * Nom du fichier DAT.
    */
   private String datFileName = null;

   /**
    * Répertoire contenant les ROMs.
    */
   private String path = null;

   /**
    * Système scanné (libellé court).
    */
   private String system = null;

   /**
    * Fichier DAT.
    */
   private Datafile datafile = null;

   /**
    * Constructeur privé car cette classe ne doit pas être instancié.
    */
   private Scanner() {
      super();
   }

   /**
    * Renvoie l'instance de la classe.
    *
    * @return L'instance de la classe.
    */
   public static Scanner getInstance() {
      return INSTANCE;
   }

   /**
    * Modifie le chemin vers le fichier DAT.
    *
    * @param aDatFileName
    *           Le chemin vers le fichier DAT.
    */
   public void setDatFileName(final String aDatFileName) {
      this.datFileName = aDatFileName;
   }

   /**
    * Modifie le chemin vers les ROMs.
    *
    * @param aPath
    *           Le chemin vers les ROMs.
    */
   public void setPath(final String aPath) {
      this.path = aPath;
   }

   /**
    * Modifie le libellé court du système.
    *
    * @param aSystem
    *           Le libellé court du système.
    */
   public void setSystem(final String aSystem) {
      this.system = aSystem;
   }

   /**
    * Scanner le répertoire des ROMs pour renommer les fichiers.
    *
    * @throws JasException
    *            L'exception levée lors du scan.
    */
   public void scan() throws JasException {
      // On parse le fichier DAT.
      datafile = new Datafile(datFileName);
      datafile.parseDatafile();

      // On parcours le répertoire (pas de récursivité).
      final File[] files = new File(path).listFiles();

      if (files != null) {
         for (int i = 0; i < files.length; i++) {
            final File file = files[i];

            System.out.println("Scan du fichier " + file.getName() + " ...");

            // Si c'est bien un fichier on le scanne.
            if (file.isFile()) {
               scanFile(file);
            }
         }

         // On indique à la classe gérant la liste d'attente que le scan est
         // fini.
         ZipQueue.getInstance().scanningFinished();

         // Génération des fichiers have/miss/dupe.
         TextGenerator.getInstance().generateHave(path, system,
               datafile.getSystem(), datafile.getListGamesHave(),
               datafile.getTotalGames());

         TextGenerator.getInstance().generateMiss(path, system,
               datafile.getSystem(), datafile.getListGamesMiss(),
               datafile.getTotalGames());

         TextGenerator.getInstance().generateDupe(path, system,
               datafile.getSystem(), datafile.getMapGamesDupes());

         // On attend la fin du thread de compression.
         while (!ZipQueue.getInstance().zipThreadFinished()) {
            try {
               Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }

      // Affichage des statistiques de fin.
      System.out.println("You have " + datafile.getListGamesHave().size() + " "
            + datafile.getSystem() + " ROMs, "
            + datafile.getListGamesMiss().size() + " missing and "
            + datafile.getDupesCount() + " dupes.");
   }

   /**
    * Scanner un fichier.
    *
    * @param file
    *           Le fichier à scanner.
    * @throws JasException
    *            L'exception levée lors du scan du fichier.
    */
   private void scanFile(final File file) throws JasException {
      // On vérifie si c'est un fichier ZIP.
      if (ZipHelper.isZipFile(file.getAbsolutePath())) {
         // On récupère les entrées de l'archive ZIP.
         List<ZipEntry> zipEntries = ZipHelper.list(file.getAbsolutePath());

         // On vérifie qu'il existe bien une entrée ZIP au moins.
         if (zipEntries != null) {
            boolean renamed = false;
            boolean known = false;
            // Parcours des entrées ZIP.
            for (ZipEntry zipEntry : zipEntries) {
               Game game = datafile.getGame(zipEntry.getCrc());
               renamed = renamed
                     || manageFile(zipEntry.getName(), game, file
                           .getAbsolutePath());

               if (!known) {
                  known = (game != null || renamed);
               }
            }

            // Suppression du fichier ZIP si le fichier est inconnu.
            if (renamed || !known) {
               System.out.println("Suppression du fichier "
                     + file.getAbsolutePath() + "... ");
               if (!Configuration.getInstance().isTest()) {
                  file.delete();
               }
            }
         }
      } else {
         // Ce n'est pas un fichier ZIP : calcul du CRC32 du fichier.
         long crc32 = ZipHelper.getCRC32(file.getAbsolutePath());

         // Lecture des infos correspondant au CRC.
         Game game = datafile.getGame(crc32);

         // Gestion du fichier.
         manageFile(file.getAbsolutePath(), game, null);
      }
   }

   /**
    * Vérifie si l'extension du fichier est connu.
    *
    * @param filePath
    *           Le chemin du fichier à vérifier.
    * @return true si le fichier est connu, false sinon.
    */
   private boolean isExtensionKnown(final String filePath) {
      boolean result = false;

      if (filePath != null && datafile.getListKnownExtensions() != null) {
         for (int i = 0; (i < datafile.getListKnownExtensions().size())
               && (!result); i++) {
            result = filePath.toLowerCase(Locale.getDefault()).endsWith(
                  (String) datafile.getListKnownExtensions().get(i)
                        .toLowerCase(Locale.getDefault()));
         }
      }

      return result;
   }

   /**
    * Permet de gérer un fichier/une entrée ZIP et d'effectuer les actions
    * nécessaires.
    *
    * @param filePath
    *           Le chemin du fichier ou le nom de l'entrée ZIP.
    * @param game
    *           Le jeu correspondant au fichier.
    * @param zipPath
    *           Le chemin vers l'archive ZIP de l'entrée.
    * @return true si le fichier a été renommé, false sinon.
    * @throws JasException
    *            L'exception levée lors de la gestion du fichier.
    */
   private boolean manageFile(final String filePath, final Game game,
         final String zipPath) throws JasException {
      // Indique si le fichier a été renommé.
      boolean renamed = false;

      // On détermine si il s'agit d'une archive ZIP ou non.
      boolean zip = (zipPath != null);

      if (game == null) {
         System.out.println(filePath + " est inconnu.");

         // Si l'extension est connue on décompresse l'entrée.
         if (isExtensionKnown(filePath)) {
            // On décompresse le fichier uniquement si c'est une archive ZIP.
            if (zip) {
               ZipHelper.unzip(zipPath, filePath);
            }
         } else {
            if (!zip) {
               System.out.println("Suppression de " + filePath + " ...");

               // Si on n'est pas en test => suppression effective.
               if (!Configuration.getInstance().isTest()) {
                  new File(filePath).delete();
               }
            }
         }
      } else {
         File file = null;

         // Si c'est une archive ZIP.
         if (zip) {
            // On vérifie que le nom de l'entrée ZIP soit correct.
            if (!filePath.equals(game.getRomName(true))) {
               // Si elle n'est pas correcte on décompresse l'entrée.
               file = new File(ZipHelper.unzip(zipPath, filePath));
            }
         } else {
            // Ce n'est pas une archive ZIP.
            file = new File(filePath);
         }

         // Si on n'a bien un fichier.
         if (file != null) {
            // On teste la concordance des deux noms.
            if (!file.getName().equals(game.getRomName(true))) {
               System.out.println("Renommage du fichier " + file.getName()
                     + " en " + game.getRomName(true));

               if (!Configuration.getInstance().isTest()) {
                  // Renommage du fichier.
                  File fileDest = new File(file.getParent()
                        + Constants.FILE_SEPARATOR + game.getRomName(true));
                  renamed = file.renameTo(fileDest);

                  // Si le fichier a bien été renommé.
                  if (renamed) {
                     file = fileDest;
                  } else {
                     file = null;
                  }
               }
            }

            if (file != null) {
               // Ajout du fichier pour compression.
               ZipQueue.getInstance().addFileToZip(file.getAbsolutePath());
            }
         }

         // On indique que le jeu est bien possédé.
         datafile.addHaveGame(game);
      }

      return renamed;
   }
}
