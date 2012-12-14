package org.jas.datafile;

import java.util.regex.Matcher;

import org.jas.commun.Configuration;
import org.jas.commun.JasException;

/**
 * Classe représentant un jeu et ses informations.
 */
public final class Game implements Comparable<Game> {
   /**
    * Nombre de caractères du numéro de release.
    */
   private static final int RELEASE_NUMBER_FORMATTED_LENGTH = 4;

   /**
    * Caractère à utiliser pour formater le numéro de release.
    */
   private static final String RELEASE_NUMBER_FORMATTED_FILLER = "0";

   /**
    * Regex pour le numéro de release.
    */
   private static final String RELEASE_NUMBER_REGEX = "%u";

   /**
    * Regex pour le titre.
    */
   private static final String TITLE_REGEX = "%n";

   /**
    * Regex pour la location.
    */
   private static final String LOCATION_REGEX = "%o";

   /**
    * Regex pour le langage.
    */
   private static final String LANGUAGE_REGEX = "%m";
   
   /**
    * Regex pour le commentaire.
    */
   private static final String COMMENT_REGEX = "%e";

   /**
    * Caractère représentant le 1 binaire.
    */
   private static final char BINARY_ONE = '1';

   /**
    * Chaîne de début permettant l'ouverture (pour la langue et la location).
    */
   private static final String START_STRING = "(";

   /**
    * Chaîne de début permettant la fermeture (pour la langue et la location).
    */
   private static final String END_STRING = ")";

   /**
    * Chaîne pour les jeux à langue unique.
    */
   private static final String UNIQUE_LANGUAGE = "";

   /**
    * Chaîne pour les jeux à langues multiples.
    */
   private static final String MULTI_LANGUAGE = "M";

   /**
    * Caractère pour les langues inconnues.
    */
   private static final char UNKNOWN_LANGUAGE = '?';

   /**
    * Caractère séparant la valeur numérique et alphanumérique de la location.
    */
   private static final String LOCATION_DEFINITION = "-";

   /**
    * Numéro de release.
    */
   private int releaseNumber = -1;

   /**
    * Titre du jeu.
    */
   private String title = null;

   /**
    * Pays de la ROM.
    */
   private long location = -1;

   /**
    * Langue(s) de la ROM.
    */
   private long language = -1;

   /**
    * CRC de la ROM.
    */
   private long romCRC = -1;

   /**
    * Identifiant des duplicatas.
    */
   private int duplicateID = -1;

   /**
    * Extension de la ROM.
    */
   private String extension = null;

   /**
    * Format de nommage de la ROM.
    */
   private String romTitle = null;

   /**
    * Nom de la ROM.
    */
   private String romName = null;
   
   /**
    * Commentaire de la ROM.
    */
   private String comment = null;

   /**
    * Renvoie l'ID de duplicata de la ROM.
    *
    * @return L'ID de duplicata de la ROM.
    */
   public int getDuplicateID() {
      return duplicateID;
   }

   /**
    * Modifie l'ID de doublon.
    *
    * @param aDuplicateID
    *           L'ID de doublon.
    */
   public void setDuplicateID(final int aDuplicateID) {
      this.duplicateID = aDuplicateID;
   }

   /**
    * Modifie le langage.
    *
    * @param aLanguage
    *           Le langage.
    */
   public void setLanguage(final long aLanguage) {
      this.language = aLanguage;
   }

   /**
    * Modifie la location.
    *
    * @param aLocation
    *           La location.
    */
   public void setLocation(final long aLocation) {
      this.location = aLocation;
   }

   /**
    * Modifie le numéro de release.
    *
    * @param aReleaseNumber
    *           Le numéro de release.
    */
   public void setReleaseNumber(final int aReleaseNumber) {
      this.releaseNumber = aReleaseNumber;
   }

   /**
    * Renvoie le crc32 de la ROM.
    * @return Le crc32 de la ROM.
    */
   public long getRomCRC() {
      return romCRC;
   }

   /**
    * Modifie le crc32 de la ROM.
    *
    * @param aRomCRC
    *           Le crc32 de la ROM.
    */
   public void setRomCRC(final long aRomCRC) {
      this.romCRC = aRomCRC;
   }

   /**
    * Modifie le titre du jeu.
    *
    * @param aTitle
    *           Le titre du jeu.
    */
   public void setTitle(final String aTitle) {
      this.title = aTitle;
   }

   /**
    * Retourne le nom de la ROM formaté.
    *
    * @param includeExtension
    *           Indique si il faut inclure l'extension ou non.
    * @return Le nom de la ROM formaté.
    */
   public String getRomName(final boolean includeExtension) {
      String result = null;

      if (romName == null) {
         try {
            romName = romTitle.replaceAll(RELEASE_NUMBER_REGEX, Matcher
                  .quoteReplacement(getFormattedReleaseNumber()));
            romName = romName.replaceAll(TITLE_REGEX, Matcher
                  .quoteReplacement(title));
            romName = romName.replaceAll(LOCATION_REGEX, Matcher
                  .quoteReplacement(getFormattedLocation()));
            romName = romName.replaceAll(LANGUAGE_REGEX, Matcher
                  .quoteReplacement(getFormattedLanguage()));
            romName = romName.replaceAll(COMMENT_REGEX, Matcher
                  .quoteReplacement(getComment()));
         } catch (JasException e) {
            e.printStackTrace();
         }
      }

      result = romName;

      // Ajout de l'extension.
      if (includeExtension) {
         result += extension;
      }
      return result;
   }

   /**
    * Renvoie le numéro de release formaté.
    *
    * @return Le numéro de release formaté.
    */
   private String getFormattedReleaseNumber() {
      String result = String.valueOf(releaseNumber);

      while (result.length() < RELEASE_NUMBER_FORMATTED_LENGTH) {
         result = RELEASE_NUMBER_FORMATTED_FILLER + result;
      }

      return result;
   }

   /**
    * Renvoie le langage formaté.
    *
    * @return Le langage formaté.
    * @throws JasException
    *            L'exception levée lors du formatage du langage.
    */
   private String getFormattedLanguage() throws JasException {
      String formattedLanguage = null;

      // On transforme le langage en nombre binaire.
      String binaryLanguage = Long.toBinaryString(language);

      // On compte le nombre de 1.
      int count = 0;
      for (int i = 0; i < binaryLanguage.length(); i++) {
         if (binaryLanguage.charAt(i) == BINARY_ONE) {
            count++;
         }
      }

      // Langue unique ?
      if (count == 1) {
         // On récupère la position du 1 binaire.
         int index = binaryLanguage.indexOf(BINARY_ONE);

         // On récupère la chaîne représentant les langues.
         String languages = Configuration.getInstance().get(
               Configuration.LANGUAGES_PROPERTY);

         // Génération de l'index de la langue.
         index += languages.length() - binaryLanguage.length();

         // Récupération du langage.
         char lang = languages.charAt(index);

         if (lang == UNKNOWN_LANGUAGE) {
            System.out.println("Langage inconnu : " + language
                  + " pour la release n°" + getFormattedReleaseNumber());
            throw new JasException();
         }

         // On renvoie une chaîne car seule la location sera affichée.
         formattedLanguage = UNIQUE_LANGUAGE;
      } else if (count > 0) {
         // Multi.
         formattedLanguage = START_STRING + MULTI_LANGUAGE + count + END_STRING;
      }

      return formattedLanguage;
   }

   /**
    * Renvoie la location formatée.
    *
    * @return La location formatée.
    * @throws JasException
    *            L'exception levée lors du formatage de la location.
    */
   private String getFormattedLocation() throws JasException {
      String result = null;

      // On récupère les locations
      String locations = Configuration.getInstance().get(
            Configuration.LOCATIONS_PROPERTY);

      final String locationString = location + LOCATION_DEFINITION;
      int beginIndex = locations.indexOf(locationString);

      if (beginIndex == -1) {
         System.out.println("Location inconnue : " + location
               + " pour la release n°" + getFormattedReleaseNumber());
         throw new JasException();
      } else {
         int endIndex = locations.substring(beginIndex).indexOf(';');

         if (endIndex == -1) {
            result = START_STRING
                  + locations.substring(beginIndex + locationString.length())
                  + END_STRING;
         } else {
            result = START_STRING
                  + locations.substring(beginIndex + locationString.length(),
                        beginIndex + endIndex) + END_STRING;
         }
      }

      return result;
   }

   /**
    * Modifie le format de nommage de la ROM.
    *
    * @param aRomTitle
    *           Le format de nommage de la ROM.
    */
   public void setRomTitle(final String aRomTitle) {
      this.romTitle = aRomTitle;
   }

   /**
    * Modifie l'extension de la ROM.
    *
    * @param aExtension
    *           L'extension de la ROM.
    */
   public void setExtension(final String aExtension) {
      this.extension = aExtension;
   }

   /**
    * Comparer l'objet o à cette instance.
    *
    * @param o
    *           L'objet à comparer.
    * @return -1 si cette instance est inférieure à l'objet, 0 si ils sont égaux
    *         et 1 sinon.
    */
   public int compareTo(final Game o) {
      int result = 0;

      if (o == this) {
         result = 0;
      } else {
         if ((o != null) && (o.getClass() == this.getClass())) {
            Game game = (Game) o;

            if (releaseNumber < game.releaseNumber) {
               result = -1;
            } else if (releaseNumber > game.releaseNumber) {
               result = 1;
            }
         }
      }

      return result;
   }

   /**
    * Renvoie le jeu sous forme de chaîne de caractères.
    *
    * @return le jeu sous forme de chaîne de caractères.
    */
   public String toString() {
      return getRomName(false);
   }
   
   /**
    * Modifier le commentaire de la ROM.
    *
    * @param aComment
    *           Le commentaire de la ROM.
    */
   public void setComment(final String aComment) {
      comment = aComment;
   }
   
   /**
    * Renvoie le commentaire lié à la ROM.
    *
    * @return Le commentaire lié à la ROM ou une chaîne vide si il n'y a pas de
    *         commentaire.
    */
   private String getComment() {
      if (comment == null)
         comment = "";
      
      return comment;
   }
}
