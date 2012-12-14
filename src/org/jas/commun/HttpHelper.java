package org.jas.commun;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Cette classe permet de simplifier la manipulation de fichiers http.
 */
public final class HttpHelper {
   /**
    * Taille du buffer pour le téléchargement sur Internet.
    */
   private static final int BUFFER_SIZE = 4 * 1024 * 1024;

   // Si on doit utiliser un proxy on initialise les propriétés nécessaires ici.
   static {
      try {
         if (Configuration.getInstance().useProxy()) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost",
                  Configuration.getInstance().getProxyHost());
            System.getProperties().put("proxyPort",
                  Configuration.getInstance().getProxyPort());

            // Ajout des informations d'authentification si elles existent.
            final String username = Configuration.getInstance()
                  .getProxyUsername();
            if (username != null) {
               // Instanciation de l'authentification.
               final PasswordAuthenticator authenticator =
                  new PasswordAuthenticator();
               authenticator.setUsername(username);
               authenticator.setPassword(Configuration.getInstance()
                     .getProxyPassword());
               Authenticator.setDefault(authenticator);
            }
         }
      } catch (JasException e) {
         e.printStackTrace();
      }
   }

   /**
    * Constructeur par défaut, privé car la classe ne doit pas être instanciée.
    */
   private HttpHelper() {
      super();
   }

   /**
    * Permet de lire le contenu d'un fichier http.
    *
    * @param fileUrl
    *           L'adresse http du fichier.
    * @return Le contenu du fichier http.
    * @throws JasException
    *            L'exception levée lors de la lecture du fichier http.
    */
   public static String readContent(final String fileUrl) throws JasException {
      String result = null;

      try {
         // Création d'une url vers le fichier à lire.
         final URL url = new URL(fileUrl);

         // Création de la connexion à l'url.
         final URLConnection urlConnection = url.openConnection();
         urlConnection.setUseCaches(false);

         // Ouverture du flux de lecture.
         final InputStreamReader inputStreamReader = new InputStreamReader(
               urlConnection.getInputStream());
         final BufferedReader bufferedReader = new BufferedReader(
               inputStreamReader);

         // Lecture du contenu.
         result = bufferedReader.readLine();

         // Fermeture du flux de lecture.
         bufferedReader.close();
         inputStreamReader.close();
      } catch (MalformedURLException e) {
         throw new JasException(e);
      } catch (IOException e) {
         throw new JasException(e);
      }

      return result;
   }

   /**
    * Permet de télécharger localement un fichier http.
    *
    * @param fileUrl
    *           Le fichier http à télécharger.
    * @param destination
    *           Le fichier local utilisé pour l'enregistrement.
    * @throws JasException
    *            L'exception levée lors du téléchargement du fichier.
    */
   public static void download(final String fileUrl, final String destination)
         throws JasException {
      try {
         // Création d'une url vers le fichier à lire.
         final URL url = new URL(fileUrl);

         // Création de la connexion à l'url.
         final URLConnection urlConnection = url.openConnection();
         urlConnection.setUseCaches(false);

         // Ouverture du flux de lecture.
         final BufferedInputStream bis = new BufferedInputStream(urlConnection
               .getInputStream());

         // Ouverture du fichier à écrire.
         final FileOutputStream fos = new FileOutputStream(destination);
         final BufferedOutputStream bos = new BufferedOutputStream(fos);

         // Initialisation du buffer de lecture.
         final byte[] buffer = new byte[BUFFER_SIZE];

         // Parcours des données à écrire.
         int read = 0;
         while (read != -1) {
            read = bis.read(buffer, 0, BUFFER_SIZE);

            if (read > 0) {
               bos.write(buffer, 0, read);
            }
         }

         // Fermeture du flux d'écriture.
         bos.close();
         fos.close();

         // Fermeture du flux de lecture.
         bis.close();
      } catch (MalformedURLException e) {
         throw new JasException(e);
      } catch (IOException e) {
         throw new JasException(e);
      }
   }
}
