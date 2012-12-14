package org.jas.zip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jas.commun.Configuration;
import org.jas.commun.JasException;

/**
 * Cette classe permet de gérer une file d'attente contenant la liste des
 * fichiers à compresser. Elle est utilisée comme un thread non-bloquant,
 * ce qui permet au scanner de continuer son analyse pendant la compression
 * des fichiers.
 */
public final class ZipThread extends Thread {
   /**
    * Temps d'attente du thread lors du parcours des fichiers à compresser.
    */
   private static final long SLEEP_TIME = 25;

   /**
    * Liste des fichiers à compresser.
    */
   private List<String> listFilesToZip = new ArrayList<String>();

   /**
    * Indique si le scanner est toujours en cours d'exécution.
    */
   private boolean scanning = false;

   /**
    * Démarrage du thread.
    */
   public void run() {
      scanning = true;
      String filename = null;

      // Tant que le scanner est en cours d'exécution.
      while (scanning) {
         // On récupère le prochain fichier à compresser.
         filename = getNextFileToZip();

         // Si il y a bien un fichier à compresser.
         if (filename != null) {
            zip(filename);
         } else {
            // Sinon on fait une pause.
            try {
               sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }

      // Le scanner a terminé son exécution, on vide la liste des fichiers.
      while ((filename = getNextFileToZip()) != null) {
         zip(filename);
      }
   }

   /**
    * Indique que le scanner a terminé son exécution.
    */
   public void scanningFinished() {
      scanning = false;
   }

   /**
    * Ajoute un fichier à la liste des fichiers à compresser.
    *
    * @param filename
    *           Le fichier à compresser.
    */
   public void addFileToZip(final String filename) {
      synchronized (listFilesToZip) {
         listFilesToZip.add(filename);
      }
   }

   /**
    * Renvoie le prochain fichier à compresser.
    *
    * @return Le prochain fichier à compresser.
    */
   private String getNextFileToZip() {
      String result = null;

      synchronized (listFilesToZip) {
         if (listFilesToZip != null && listFilesToZip.size() > 0) {
            result = (String) listFilesToZip.get(0);
         }
      }

      return result;
   }

   /**
    * Permet de supprimer un fichier de la liste des fichiers à compresser.
    *
    * @param filename
    *           Le fichier à supprimer.
    */
   private void removeFileToZip(final String filename) {
      synchronized (listFilesToZip) {
         listFilesToZip.remove(filename);
      }
   }

   /**
    * Permet de compresser un fichier.
    *
    * @param filename
    *           Le chemin du fichier à compresser.
    */
   private void zip(final String filename) {
      try {
         System.out.println("Compression du fichier " + filename);

         // Si on n'est pas en test => compression effective.
         if (!Configuration.getInstance().isTest()) {
            ZipHelper.zip(filename);
            new File(filename).delete();
         }
      } catch (JasException e) {
         e.printStackTrace();
      } finally {
         // Suppression du fichier de la liste d'attente dans tous les cas.
         removeFileToZip(filename);
      }
   }
}
