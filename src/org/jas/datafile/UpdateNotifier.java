package org.jas.datafile;

import java.util.ArrayList;
import java.util.List;

import org.jas.commun.HttpHelper;
import org.jas.commun.JasException;

/**
 * Cette classe permet de gérer la mise à jour du fichier DAT.
 */
public final class UpdateNotifier {
   /**
    * Instance unique de la classe.
    */
   private static final UpdateNotifier INSTANCE = new UpdateNotifier();

   /**
    * Liste des classes abonnées aux notifications.
    */
   private List<UpdateListener> updateListeners = null;

   /**
    * Thread permettant de détecter et éventuellement de télécharger la dernière
    * version du DAT. On utilise un thread pour ne pas bloquer le parsing XML.
    */
   private class UpdateThread extends Thread {
      /**
       * Url du fichier contenant le numéro de version.
       */
      private String datVersionURL = null;

      /**
       * Numéro de version actuel du DAT.
       */
      private String datVersion = null;

      /**
       * Url du fichier DAT.
       */
      private String datUrl = null;

      /**
       * Chemin vers le fichier DAT.
       */
      private String datPath = null;

      /**
       * Modifie le chemin vers le fichier DAT.
       *
       * @param aDatPath
       *           Le chemin vers le fichier DAT.
       */
      public void setDatPath(final String aDatPath) {
         this.datPath = aDatPath;
      }

      /**
       * Modifie l'url du fichier DAT.
       *
       * @param aDatUrl
       *           L'url du fichier DAT.
       */
      public void setDatUrl(final String aDatUrl) {
         this.datUrl = aDatUrl;
      }

      /**
       * Modifie la version actuelle du DAT.
       *
       * @param aDatVersion
       *           La version actuelle du DAT.
       */
      public void setDatVersion(final String aDatVersion) {
         this.datVersion = aDatVersion;
      }

      /**
       * Modifie l'url du fichier contenant le numéro de la dernière version du
       * DAT.
       *
       * @param aDatVersionURL
       *           L'url du fichier contenant le numéro de la dernière version
       *           du DAT.
       */
      public void setDatVersionURL(final String aDatVersionURL) {
         this.datVersionURL = aDatVersionURL;
      }

      /**
       * Démarrage du thread.
       */
      public void run() {
         try {
            // Lecture du dernier numéro de version.
            String latestVersion = null;
            try {
               latestVersion = HttpHelper.readContent(datVersionURL);
            } catch (JasException e) {
               System.out.println("Impossible de vérifier la version du DAT.");
            }

            // Si il est différent.
            if (latestVersion != null && !latestVersion.equals(datVersion)) {
               System.out
                     .println("Une nouvelle version du DAT est disponible.");
               notifyNewVersionAvailable();

               System.out.println("Téléchargement du fichier " + datUrl
                     + " ...");
               HttpHelper.download(datUrl, datPath);
               notifyDownloadFinished();
            } else {
               notifyNoNewVersionAvailable();
            }
         } catch (JasException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Constructeur privé, la classe ne doit pas être instanciée.
    */
   private UpdateNotifier() {
      super();
   }

   /**
    * Renvoie l'instance unique de la classe.
    *
    * @return L'instance unique de la classe.
    */
   public static UpdateNotifier getInstance() {
      return INSTANCE;
   }

   /**
    * Permet de vérifier si une nouvelle version du DAT est disponible.
    *
    * @param datVersion
    *           Le numéro de version actuelle du DAT.
    * @param datVersionURL
    *           L'url vers le fichier contenant le numéro de la dernière version
    *           du DAT.
    * @param datUrl
    *           L'url du DAT à télécharger en cas de mise à jour.
    * @param datPath
    *           Le chemin du fichier DAT.
    * @throws JasException
    *            L'exception levée lors de la vérification du DAT.
    */
   public void checkUpdate(final String datVersion, final String datVersionURL,
         final String datUrl, final String datPath) throws JasException {
      // Démarrage du thread de mise à jour.
      UpdateThread updateThread = new UpdateThread();
      updateThread.setDatVersion(datVersion);
      updateThread.setDatVersionURL(datVersionURL);
      updateThread.setDatUrl(datUrl);
      updateThread.setDatPath(datPath);
      updateThread.start();
   }

   /**
    * Permet d'abonner une classe aux évènements.
    *
    * @param listener
    *           La classe à abonner.
    */
   public void addUpdateListener(final UpdateListener listener) {
      if (updateListeners == null) {
         updateListeners = new ArrayList<UpdateListener>();
      }

      updateListeners.add(listener);
   }

   /**
    * Indique qu'une nouvelle version du DAT est disponible.
    */
   private void notifyNewVersionAvailable() {
      for (int i = 0; i < updateListeners.size(); i++) {
         UpdateListener listener = (UpdateListener) updateListeners.get(i);
         listener.newVersionAvailable();
      }
   }

   /**
    * Indique que le téléchargement de la dernière version du DAT est terminé.
    */
   private void notifyDownloadFinished() {
      for (int i = 0; i < updateListeners.size(); i++) {
         UpdateListener listener = (UpdateListener) updateListeners.get(i);
         listener.downloadFinished();
      }
   }

   /**
    * Indique qu'aucune nouvelle version du DAT n'est disponible.
    */
   private void notifyNoNewVersionAvailable() {
      for (int i = 0; i < updateListeners.size(); i++) {
         UpdateListener listener = (UpdateListener) updateListeners.get(i);
         listener.noNewVersionAvailable();
      }
   }
}
