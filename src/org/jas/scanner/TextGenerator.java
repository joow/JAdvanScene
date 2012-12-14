package org.jas.scanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jas.commun.Constants;
import org.jas.commun.JasException;
import org.jas.datafile.Game;

/**
 * Cette classe permet de générer un fichier texte en fonction d'une liste de
 * jeux.
 */
public final class TextGenerator {
   /**
    * Instance unique de la classe.
    */
   private static final TextGenerator INSTANCE = new TextGenerator();

   /**
    * Extension des fichiers texte.
    */
   private static final String TXT_EXTENSION = ".txt";

   /**
    * Expression verb.
    */
   private static final String VERB_EXPRESSION = "%verb%";

   /**
    * Expression count.
    */
   private static final String COUNT_EXPRESSION = "%count%";

   /**
    * Expression total.
    */
   private static final String TOTAL_EXPRESSION = "%total%";

   /**
    * Expression system.
    */
   private static final String SYSTEM_EXPRESSION = "%system%";

   /**
    * Verbe miss.
    */
   private static final String VERB_MISS = "miss";

   /**
    * Verbe have.
    */
   private static final String VERB_HAVE = "have";

   /**
    * Verbe have.
    */
   private static final String VERB_DUPE = "dupe";

   /**
    * Séparateur system-verb.
    */
   private static final String VERB_SEPARATOR = "-";

   /**
    * Séparateur de dupes.
    */
   private static final String DUPE_SEPARATOR = " / ";

   /**
    * Entête des fichiers have/miss.
    */
   private static final String HAVE_MISS_HEADER = " You " + VERB_EXPRESSION
         + " " + COUNT_EXPRESSION + " of " + TOTAL_EXPRESSION + " known "
         + SYSTEM_EXPRESSION + " ROMs.";

   /**
    * Constructeur, privé car cette classe ne doit pas être instanciée.
    */
   private TextGenerator() {
      super();
   }

   /**
    * Renvoie l'instance unique de TextGenerator.
    *
    * @return L'instance unique de TextGenerator.
    */
   public static TextGenerator getInstance() {
      return INSTANCE;
   }

   /**
    * Génère l'header d'un fichier have/miss.
    *
    * @param verb
    *           Le verbe correspondant à l'action (have ou miss).
    * @param count
    *           Le nombre de jeux have ou miss.
    * @param total
    *           Le nombre total de jeux du DAT.
    * @param systemLabel
    *           Le libellé complet du système scanné.
    * @return L'header à inscrire en début de fichier.
    */
   private String generateHeader(final String verb, final int count,
         final int total, final String systemLabel) {
      String header = HAVE_MISS_HEADER.replaceAll(VERB_EXPRESSION, verb);
      header = header.replaceAll(COUNT_EXPRESSION, String.valueOf(count));
      header = header.replaceAll(TOTAL_EXPRESSION, String.valueOf(total));
      header = header.replaceAll(SYSTEM_EXPRESSION, systemLabel);

      return header;
   }

   /**
    * Permet de générer un fichier have.
    *
    * @param path
    *           Le répertoire où écrire le fichier.
    * @param system
    *           Le nom court du système.
    * @param systemLabel
    *           Le libellé complet du système.
    * @param listGamesHave
    *           La liste des jeux have.
    * @param totalGames
    *           Le nombre total de jeux du DAT.
    * @return Le chemin du fichier généré.
    * @throws JasException
    *            L'exception levée lors de la génération du fichier.
    */
   public String generateHave(final String path, final String system,
         final String systemLabel, final List<Game> listGamesHave,
         final int totalGames) throws JasException {
      final String header = generateHeader(VERB_HAVE, listGamesHave.size(),
            totalGames, systemLabel);

      Collections.sort(listGamesHave);

      return generate(path, system, VERB_HAVE, header, listGamesHave);
   }

   /**
    * Permet de générer un fichier miss.
    *
    * @param path
    *           Le répertoire où écrire le fichier.
    * @param system
    *           Le nom court du système.
    * @param systemLabel
    *           Le libellé complet du système.
    * @param listGamesMiss
    *           La liste des jeux miss.
    * @param totalGames
    *           Le nombre total de jeux du DAT.
    * @return Le chemin du fichier généré.
    * @throws JasException
    *            L'exception levée lors de la génération du fichier.
    */
   public String generateMiss(final String path, final String system,
         final String systemLabel, final List<Game> listGamesMiss,
         final int totalGames) throws JasException {
      final String header = generateHeader(VERB_MISS, listGamesMiss.size(),
            totalGames, systemLabel);

      Collections.sort(listGamesMiss);

      return generate(path, system, VERB_MISS, header, listGamesMiss);
   }

   /**
    * Permet de générer un fichier dupe.
    *
    * @param path
    *           Le répertoire où écrire le fichier.
    * @param system
    *           Le nom court du système.
    * @param systemLabel
    *           Le libellé complet du système.
    * @param mapGamesDupe
    *           La map des jeux dupe.
    * @return Le chemin du fichier généré.
    * @throws JasException
    *            L'exception levée lors de la génération du fichier.
    */
   public String generateDupe(final String path, final String system,
         final String systemLabel, final Map<Integer, List<Game> > mapGamesDupe) throws JasException {
      String filename = null;

      // Parcours des dupes.
      int dupesCount = 0;
      final StringBuffer dupesString = new StringBuffer();

      // Comptage du nombre de dupes et génération de la liste.
      for (List<Game> listGamesDupe :  mapGamesDupe.values()) {
         if (listGamesDupe.size() > 1) {
            dupesCount += listGamesDupe.size();

            for (int j = 0; j < listGamesDupe.size(); j++) {
               final Game game = (Game) listGamesDupe.get(j);
               dupesString.append(game.getRomName(false));

               if (j < listGamesDupe.size() - 1) {
                  dupesString.append(DUPE_SEPARATOR);
               } else {
                  dupesString.append(System.getProperty("line.separator"));
               }
            }
         }
      }

      if (dupesCount > 0) {
         // Génération du nom de fichier.
         filename = path + Constants.FILE_SEPARATOR + system
               + VERB_SEPARATOR + VERB_DUPE + TXT_EXTENSION;

         try {
            final FileWriter fileWriter = new FileWriter(filename);
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(" You have " + dupesCount + " " + systemLabel + " dupes.");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.write(dupesString.toString());
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();
         } catch (IOException e) {
            throw new JasException(e);
         }
      }

      return filename;
   }

   /**
    * Génère un fichier.
    *
    * @param path
    *           Le répertoire où écrire le fichier.
    * @param system
    *           Le nom court du système.
    * @param type
    *           Le type de fichier.
    * @param header
    *           L'entête à écrire en début de fichier.
    * @param listGames
    *           La liste des jeux.
    * @return Le chemin du fichier généré.
    * @throws JasException
    *            L'exception levée lors de la génération du fichier.
    */
   private String generate(final String path, final String system,
         final String type, final String header, final List<Game> listGames)
         throws JasException {
      final String filename = path + Constants.FILE_SEPARATOR + system
            + VERB_SEPARATOR + type + TXT_EXTENSION;

      try {
         final FileWriter fileWriter = new FileWriter(filename);
         final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

         bufferedWriter.write(header);
         bufferedWriter.newLine();
         bufferedWriter.newLine();

         for (int i = 0; i < listGames.size(); i++) {
            final Game game = (Game) listGames.get(i);
            bufferedWriter.write(game.getRomName(false));
            bufferedWriter.newLine();
         }

         bufferedWriter.close();
         fileWriter.close();
      } catch (IOException e) {
         throw new JasException(e);
      }

      return filename;
   }
}
