package org.jas.commun;

/**
 * Classe regroupant les constantes pouvant être utilisée par n'importe quelle
 * classe.
 */
public final class Constants {
   /**
    * Séparateur de fichier.
    */
   public static final String FILE_SEPARATOR = System
         .getProperty("file.separator");

   /**
    * Contructeur par défaut, privée car la classe ne doit pas être instanciée.
    */
   private Constants() {
      super();
   }
}
