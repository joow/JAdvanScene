package org.jas.datafile;

/**
 * Cette interface permet de s'abonner aux notifications d'évènements envoyées
 * par l'UpdateNotifier.
 */
public interface UpdateListener {
   /**
    * Indique qu'aucune nouvelle version du DAT n'est disponible.
    */
   void noNewVersionAvailable();

   /**
    * Indique qu'une nouvelle version du DAT est disponible.
    */
   void newVersionAvailable();

   /**
    * Indique que le téléchargement de la dernière version du DAT est terminé.
    */
   void downloadFinished();
}
