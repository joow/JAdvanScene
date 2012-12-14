package org.jas;

import org.jas.commun.Configuration;
import org.jas.commun.JasException;
import org.jas.scanner.Scanner;

/**
 * Classe principale permettant de lancer le scan du sytème voulu.
 */
public final class JAdvanScene {
   /**
    * Constructeur par défaut, privé car la classe ne doit pas être instanciée
    * ailleurs que dans cette classe.
    */
   private JAdvanScene() {
      super();
   }

   /**
    * @param args
    *           Le système à scanner.
    */
   public static void main(final String[] args) {
      if (args.length == 1) {
         new JAdvanScene().scan(args[0]);
      } else {
         System.out.println("Vous devez indiquer le système à scanner.");
      }
   }

   /**
    * Lancer le scan du système voulu.
    *
    * @param system
    *           Le système à scanner.
    */
   private void scan(final String system) {
      try {
         // Lecture de la propriété contenant le chemin vers le DAT.
         final String datFileNameProperty = system + ".dat";
         final String datFileName = Configuration.getInstance().get(
               datFileNameProperty);

         if (datFileName == null) {
            System.out.println(
                  "Vous devez configurer le chemin vers le fichier DAT.");
         } else {
            // Lecture de la propriété contenant le chemin vers les ROMs.
            final String pathProperty = system + ".path";
            final String path = Configuration.getInstance().get(pathProperty);

            if (path == null) {
               System.out
                     .println("Vous devez configurer le chemin vers les ROMs.");
            } else {
               // Démarrage du scan.
               final Scanner scanner = Scanner.getInstance();
               scanner.setDatFileName(datFileName);
               scanner.setPath(path);
               scanner.setSystem(system);
               scanner.scan();
            }
         }
      } catch (JasException e) {
         e.printStackTrace();
      }
   }
}
