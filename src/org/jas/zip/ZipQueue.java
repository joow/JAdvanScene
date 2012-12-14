package org.jas.zip;

/**
 * Classe permettant de gérer la liste des fichiers en attente de compression.
 * Elle s'appuie pour cela sur la classe ZipThread qui permet de déléguer la
 * compression des fichiers à un thread non-bloquant.
 */
public final class ZipQueue {
   /**
    * Instance unique de la classe.
    */
   private static final ZipQueue INSTANCE = new ZipQueue();

   /**
    * Thread gérant la compression des fichiers ZIP.
    */
   private static ZipThread zipThread = null;

   /**
    * Constructeur par défaut, privé car cette classe ne doit pas être
    * instanciée.
    */
   private ZipQueue() {
      super();
   }

   /**
    * Renvoie l'instance de la classe.
    *
    * @return L'instance de la classe.
    */
   public static ZipQueue getInstance() {
      return INSTANCE;
   }

   /**
    * Ajoute un fichier à compresser à la liste d'attente.
    *
    * @param filename
    *           Le chemin du fichier à compresser.
    */
   public void addFileToZip(final String filename) {
      // On instancie le thread de compression si cela n'a pas encore été fait.
      if (zipThread == null) {
         zipThread = new ZipThread();
         zipThread.start();
      }

      zipThread.addFileToZip(filename);
   }

   /**
    * Indique que le scanner a terminé son exécution.
    */
   public void scanningFinished() {
      // Si le thread de compression existe on lui notifie la fin du scan.
      if (zipThread != null) {
         zipThread.scanningFinished();
      }
   }

   /**
    * Permet de savoir si le thread de compression est toujours actif.
    *
    * @return true si le thread de compression est actif, false sinon.
    */
   public boolean zipThreadFinished() {
      boolean result = true;

      if (zipThread != null) {
         result ^= zipThread.isAlive();
      }

      return result;
   }
}
