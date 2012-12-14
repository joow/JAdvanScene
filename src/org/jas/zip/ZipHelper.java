package org.jas.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.jas.commun.Constants;
import org.jas.commun.JasException;

/**
 * Cette classe permet de faciliter la manipulation des archives ZIP.
 */
public final class ZipHelper {
   /**
    * Extension pour les fichiers ZIP.
    */
   private static final String ZIP_EXTENSION = ".zip";

   /**
    * Taille du buffer de lecture/écriture.
    */
   private static final int BUFFER_SIZE = 8 * 1024 * 1024;

   /**
    * Constructeur par défaut, privé car la classe ne doit pas être instanciée.
    */
   private ZipHelper() {
      super();
   }

   /**
    * Renvoie le répertoire d'un chemin de fichier.
    *
    * @param filePath
    *           Le chemin du fichier.
    * @return Le répertoire contenant le fichier.
    */
   private static String getFolderPathFromFilePath(final String filePath) {
      File file = new File(filePath);
      return file.getParent();
   }

   /**
    * Permet de décompresser une archive zip.
    *
    * @param filePath
    *           Le chemin de l'archive ZIP à décompresser.
    * @return La liste des fichiers décompressés (avec leur chemin) ou null si
    *         aucun fichier n'a été décompressé.
    * @throws JasException
    *            L'exception levée lors de la décompression du fichier.
    */
   public static List<String> unzip(final String filePath) throws JasException {
      List<String> result = null;

      // Détermination du répertoire.
      String folderPath = getFolderPathFromFilePath(filePath);

      try {
         // Ouverture de l'archive ZIP.
         ZipFile zipFile = new ZipFile(filePath);

         // Parcours des entrées de l'archive.
         Enumeration<? extends ZipEntry> entries = zipFile.entries();
         while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();

            // Initialisation de la liste des résultats.
            if (result == null) {
               result = new ArrayList<String>();
            }

            // Décompression de l'entrée et ajout du fichier au résultat.
            result.add(unzip(zipFile, zipEntry, folderPath));
         }

         // Fermeture de l'archive ZIP.
         zipFile.close();
      } catch (IOException e) {
         throw new JasException(e);
      }

      return result;
   }

   /**
    * Permet de décompresser une entrée d'une archive zip.
    *
    * @param filePath
    *           Le chemin de l'archive ZIP à décompresser.
    * @param filename
    *           Le nom de l'entrée à décompresser.
    * @return Le chemin du fichier décompressé ou null si aucun fichier n'a été
    *         décompressé.
    * @throws JasException
    *            L'exception levée lors de la décompression du fichier.
    */
   public static String unzip(final String filePath, final String filename)
         throws JasException {
      String result = null;

      // Détermination du répertoire.
      String folderPath = getFolderPathFromFilePath(filePath);

      try {
         // Ouverture de l'archive ZIP.
         ZipFile zipFile = new ZipFile(filePath);

         // Parcours des entrées de l'archive.
         Enumeration<? extends ZipEntry> entries = zipFile.entries();
         while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();

            if (zipEntry.getName().equals(filename)) {
               // Décompression de l'entrée.
               result = unzip(zipFile, zipEntry, folderPath);
            }
         }

         // Fermeture de l'archive ZIP.
         zipFile.close();
      } catch (IOException e) {
         throw new JasException(e);
      }

      return result;
   }

   /**
    * Permet de lister le contenu d'une archive zip.
    *
    * @param filePath
    *           Le chemin de l'archive ZIP.
    * @return La liste des fichiers contenus dans l'archive.
    * @throws JasException
    *            L'exception levée lors du parcours de l'archive.
    */
   public static List<ZipEntry> list(final String filePath)
         throws JasException {
      List<ZipEntry> result = null;

      try {
         // Ouverture de l'archive ZIP.
         ZipFile zipFile = new ZipFile(filePath);

         // Parcours des entrées de l'archive.
         Enumeration<? extends ZipEntry> entries = zipFile.entries();
         while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();

            // Initialisation de la liste des résultats.
            if (result == null) {
               result = new ArrayList<ZipEntry>();
            }

            result.add(zipEntry);
         }

         // Fermeture de l'archive ZIP.
         zipFile.close();
      } catch (IOException e) {
         throw new JasException(e);
      }

      return result;
   }

   /**
    * Permet de décompresser une entrée d'une archive ZIP.
    *
    * @param zipFile
    *           L'archive ZIP.
    * @param zipEntry
    *           L'entrée de l'archive ZIP à décompresser.
    * @param folderPath
    *           Le répertoire de destination du fichier décompressé.
    * @return Le nom du fichier décompressé.
    * @throws JasException
    *            L'exception levée lors de la décompression de l'entrée.
    */
   private static String unzip(final ZipFile zipFile, final ZipEntry zipEntry,
         final String folderPath) throws JasException {
      String result = null;

      // On vérifie si l'entrée est un répertoire.
      if (zipEntry.isDirectory()) {
         // Dans ce cas on vérifie si il faut le créer.
         File file = new File(folderPath + Constants.FILE_SEPARATOR
               + zipEntry.getName());

         // Si le répertoire n'existe pas on le créé (ainsi que les parents).
         if (!file.exists()) {
            file.mkdirs();
         }
      } else {
         try {
            // Ouverture du buffer de lecture de l'entrée à décompresser.
            BufferedInputStream bis = new BufferedInputStream(zipFile
                  .getInputStream(zipEntry));

            // Génération du nom du fichier de destination.
            result = folderPath + Constants.FILE_SEPARATOR + zipEntry.getName();

            // Ouverture du fichier de destination.
            FileOutputStream fos = new FileOutputStream(result);

            // Ouverture du buffer d'écriture.
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // Initialisation du buffer.
            byte[] buffer = new byte[BUFFER_SIZE];

            int read = 0;

            // Parcours des données à écrire.
            while ((read = bis.read(buffer)) != -1) {
               // Ecriture des données lues.
               bos.write(buffer, 0, read);
            }

            // Fermeture du buffer d'écriture.
            bos.close();

            // Fermeture du fichier de destination.
            fos.close();

            // Fermeture du buffer de lecture.
            bis.close();
         } catch (IOException e) {
            throw new JasException(e);
         }
      }

      return result;
   }

   /**
    * Permet de créer une archive ZIP à partir d'un fichier. L'archive ZIP
    * portera le même nom que le fichier avec l'extension ZIP à la place de
    * l'extension du fichier
    *
    * @param path
    *           Le chemin du fichier à compresser.
    * @throws JasException
    *            L'exception levée lors de la compression du fichier.
    */
   public static void zip(final String path) throws JasException {
      try {
         // Ouverture du fichier à compresser.
         File fileSource = new File(path);

         // Génération du nom du fichier ZIP.
         String zipFilename = fileSource.getParent() + Constants.FILE_SEPARATOR
               + getBaseName(fileSource.getName()) + ZIP_EXTENSION;

         // Création de l'archive ZIP.
         FileOutputStream fos = new FileOutputStream(zipFilename);
         ZipOutputStream zos = new ZipOutputStream(fos);

         // Définition du niveau de compression.
         zos.setLevel(Deflater.BEST_COMPRESSION);

         // Création d'une nouvelle entrée.
         zos.putNextEntry(createZipEntry(path));

         // Compression.
         zip(zos, fileSource);

         // Fermeture de l'entrée.
         zos.closeEntry();

         // Fermeture de l'archive ZIP.
         zos.close();
         fos.close();
      } catch (IOException e) {
         throw new JasException(e);
      }
   }

   /**
    * Permet de créer une nouvelle entrée ZIP.
    *
    * @param filename
    *           Le nom du fichier à compresser.
    * @return Une entrée ZIP pour le fichier à compresser.
    */
   private static ZipEntry createZipEntry(final String filename) {
      // Ouverture du fichier.
      File file = new File(filename);

      // Création de la nouvelle entrée ZIP à partir du nom de fichier.
      ZipEntry zipEntry = new ZipEntry(file.getName());

      return zipEntry;
   }

   /**
    * Compresser un fichier.
    *
    * @param zipOutputStream
    *           Le flux d'écriture de l'archive ZIP.
    * @param fileSource
    *           Le fichier à compresser.
    * @throws JasException
    *            L'exception levée lors de la compression du fichier.
    */
   private static void zip(final ZipOutputStream zipOutputStream,
         final File fileSource) throws JasException {
      // Création du buffer de lecture/écriture.
      byte[] buffer = new byte[BUFFER_SIZE];

      try {
         // Ouverture du fichier source.
         FileInputStream fis = new FileInputStream(fileSource);
         BufferedInputStream bis = new BufferedInputStream(fis);

         // Parcours du fichier source.
         int read = 0;
         while ((read = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
            zipOutputStream.write(buffer, 0, read);
         }

         // Fermeture du fichier source.
         bis.close();
         fis.close();
      } catch (IOException e) {
         throw new JasException(e);
      } finally {
         // Destruction du buffer.
         buffer = null;
      }
   }

   /**
    * Permet de savoir si un fichier est une archive ZIP.
    *
    * @param filename
    *           Le nom du fichier à tester.
    * @return true si le fichier est une archive ZIP, false sinon.
    */
   public static boolean isZipFile(final String filename) {
      boolean result = false;

      // On teste l'extension du fichier.
      if (filename != null) {
         result = filename.toLowerCase().endsWith(ZIP_EXTENSION);
      }

      return result;
   }

   /**
    * Permet de calculer le crc32 d'un fichier.
    *
    * @param filename
    *           Le fichier.
    * @return Le crc32 du fichier.
    * @throws JasException
    *            L'exception levée lors du calcul du crc32 du fichier.
    */
   public static long getCRC32(final String filename) throws JasException {
      CRC32 crc32 = new CRC32();

      try {
         // Ouverture du fichier source.
         FileInputStream fis = new FileInputStream(filename);
         BufferedInputStream bis = new BufferedInputStream(fis);

         // Parcours du fichier.
         byte[] buffer = new byte[BUFFER_SIZE];
         int read = 0;
         while ((read = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
            crc32.update(buffer, 0, read);
         }

         // Fermeture du fichier.
         bis.close();
         fis.close();
      } catch (FileNotFoundException e) {
         throw new JasException(e);
      } catch (IOException e) {
         throw new JasException(e);
      }

      return crc32.getValue();
   }

   /**
    * Renvoie le basename d'un nom de fichier, c'est-à-dire son nom sans chemin
    * ni extension (/home/test.bin => test).
    *
    * @param filename
    *           Le nom du fichier.
    * @return Le nom de base du fichier.
    */
   private static String getBaseName(final String filename) {
      String result = null;

      if (filename != null) {
         // Récupération de la position de départ (séparateur de fichier)
         // et de fin (séparateur d'extension).
         int startIndex = filename.lastIndexOf(Constants.FILE_SEPARATOR);
         int endIndex = filename.lastIndexOf(".");

         if (endIndex == -1) {
            result = filename.substring(startIndex + 1);
         } else {
            result = filename.substring(startIndex + 1, endIndex);
         }
      }

      return result;
   }
}
