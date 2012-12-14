package org.jas.commun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Classe permettant de lire la configuration.
 */
public final class Configuration {
   /**
    * Répertoire de travail de l'application.
    */
   private static final String APPLICATION_FOLDER = ".jas";

   /**
    * Nom du fichier des propriétés.
    */
   private static final String PROPERTY_FILENAME = "jas.properties";

   /**
    * Header du fichier des propriétés.
    */
   private static final String PROPERTY_FILE_HEADER =
      "# Fichier de configuration de JAdvanScene";

   /**
    * Exemple de propriété pour le chemin du fichier DAT.
    */
   private static final String DAT_PROPERTY_EXAMPLE = "%system%.dat";

   /**
    * Exemple de propriété pour le chemin des ROMs.
    */
   private static final String PATH_PROPERTY_EXAMPLE = "%system%.path";

   /**
    * Nom de la propriété test.
    */
   private static final String TEST_PROPERTY = "test";

   /**
    * Valeur par défaut pour la propriété test.
    */
   private static final String TEST_PROPERTY_DEFAULT_VALUE = "yes";

   /**
    * Séparateur entre les champs du proxy.
    */
   private static final String PROXY_FIELD_SEPARATOR = ":";

   /**
    * Séparateur entre les groupes de champs du proxy.
    */
   private static final String PROXY_GROUP_SEPARATOR = "@";

   /**
    * Nom de la propriété proxy.
    */
   private static final String PROXY_PROPERY = "proxy";

   /**
    * Valeur par défaut de la propriété proxy.
    */
   private static final String PROXY_PROPERTY_DEFAULT_VALUE = "user"
         + PROXY_FIELD_SEPARATOR + "password" + PROXY_GROUP_SEPARATOR + "host"
         + PROXY_FIELD_SEPARATOR + "port";

   /**
    * Nom de la propriété languages.
    */
   public static final String LANGUAGES_PROPERTY = "languages";

   /**
    * Valeur par défaut de la propriété languages. Korea, ?, ?, Swedish,
    * Spanish, Portuguese, ?, Norwegian, Japanese, Italian, German, ?, Dutch,
    * Danish, ?, Chinese, English, French.
    */
   private static final String LANGUAGES_PROPERTY_DEFAULT_VALUE =
      "K?CSSP?NJIG?DDCEF";

   /**
    * Nom de la propriété locations.
    */
   public static final String LOCATIONS_PROPERTY = "locations";

   /**
    * Valeur par défaut de la propriété locations. Europe, USA, Germany, China,
    * Spain, France, Italy, Japan, Netherland, Australy, Korea.
    */
   private static final String LOCATIONS_PROPERTY_DEFAULT_VALUE =
      "0-E;1-U;2-G;3-C;4-S;5-F;6-I;7-J;8-N;19-A;22-K";

   /**
    * Répertoire de l'utilisateur.
    */
   private static final String USER_HOME = System.getProperty("user.home");

   /**
    * Instance de la classe Configuration.
    */
   private static Configuration instance = null;

   /**
    * Chemin vers le fichier de configuration.
    */
   private static String configurationPath = null;

   /**
    * Propriétés lues dans le fichier de configuration.
    */
   private static Properties properties = new Properties();

   /**
    * Booléen indiquant si on doit utiliser un proxy.
    */
   private Boolean useProxy = null;

   /**
    * Nom d'utilisateur pour la connexion au proxy.
    */
   private String proxyUsername = null;

   /**
    * Mot de passe pour la connexion au proxy.
    */
   private String proxyPassword = null;

   /**
    * Host du proxy.
    */
   private String proxyHost = null;

   /**
    * Port du proxy.
    */
   private String proxyPort = null;

   /**
    * Renvoie l'instance de la classe Configuration.
    *
    * @return L'instance de la classe Configuration.
    * @throws JasException
    *            L'exception levée lors de la lecture de la config.
    */
   public static Configuration getInstance() throws JasException {
      if (instance == null) {
         instance = new Configuration();

         // Initialisation de la configuration
         initialiserConfiguration();
      }

      return instance;
   }

   /**
    * Initialise la configuration en la lisant si le fichier de configuration
    * existe ou en créant le fichier de configuration s'il n'existe pas.
    *
    * @throws JasException
    *            L'exception levée lors de la lecture de la config.
    */
   private static void initialiserConfiguration() throws JasException {
      // Initialisation du chemin vers le fichier de configuration.
      configurationPath = USER_HOME + Constants.FILE_SEPARATOR
            + APPLICATION_FOLDER + Constants.FILE_SEPARATOR;

      // On ouvre le fichier de configuration.
      File configurationFile = new File(configurationPath + PROPERTY_FILENAME);

      // Si il existe on lit la configuration.
      if (configurationFile.exists()) {
         try {
            // Ouverture d'un flux de lecture pour le fichier de configuration.
            FileInputStream fileInputStream = new FileInputStream(
                  configurationFile);

            // Lecture des propriétes.
            properties.load(fileInputStream);

            // Fermeture du flux de lecture
            fileInputStream.close();
         } catch (FileNotFoundException e) {
            throw new JasException(e);
         } catch (IOException e) {
            throw new JasException(e);
         }
         } else {
            // Sinon on initialise les répertoires et la configuration.
            // Création du répertoire de travail de l'application.
            new File(configurationPath).mkdirs();

            try {
               // Création du fichier de configuration (vide).
               properties.put(DAT_PROPERTY_EXAMPLE, "");
               properties.put(PATH_PROPERTY_EXAMPLE, "");
               properties.put(TEST_PROPERTY, TEST_PROPERTY_DEFAULT_VALUE);
               properties.put(PROXY_PROPERY, PROXY_PROPERTY_DEFAULT_VALUE);
               properties
               .put(LANGUAGES_PROPERTY, LANGUAGES_PROPERTY_DEFAULT_VALUE);
               properties
               .put(LOCATIONS_PROPERTY, LOCATIONS_PROPERTY_DEFAULT_VALUE);

               FileOutputStream fileOutputStream = new FileOutputStream(
                     configurationFile);
               properties.store(fileOutputStream, PROPERTY_FILE_HEADER);
               fileOutputStream.close();
            } catch (IOException e) {
               throw new JasException(e);
            }
      }
   }

   /**
    * Renvoie la valeur de la propriété voulue.
    *
    * @param propertyName
    *           Le nom de la propriété.
    * @return La valeur de la propriété.
    */
   public String get(final String propertyName) {
      String result = null;

      if (properties != null) {
         result = properties.getProperty(propertyName);
      }

      return result;
   }

   /**
    * Indique si le programme est lancé en mode test, c'est-à-dire que les
    * opérations sur le fichier sont indiqués mais ne sont pas effectuées.
    *
    * @return true si la propriété test est à yes ou si elle n'existe pas, false
    *         sinon.
    */
   public boolean isTest() {
      boolean result = true;

      if (properties != null) {
         String testValue = properties.getProperty(TEST_PROPERTY);

         if (testValue != null) {
            result = testValue.toLowerCase()
                  .equals(TEST_PROPERTY_DEFAULT_VALUE);
         }
      }

      return result;
   }

   /**
    * Indique si il faut utiliser un proxy pour les connexions http.
    *
    * @return true si un proxy a été configuré, false sinon.
    */
   public boolean useProxy() {
      boolean result = false;

      // Si on n'a pas encore déterminé l'utilisation ou non du proxy
      if (useProxy == null) {
         // On lit la propriété.
         if (properties != null) {
            String proxy = properties.getProperty(PROXY_PROPERY);

            if (proxy != null && !proxy.equals(PROXY_PROPERTY_DEFAULT_VALUE)) {
               // On parse la valeur du proxy pour initialiser les champs
               // voulus.
               parseProxy(proxy);
               result = true;
            }
         }

         // Initialisation de la variable indiquant l'utilisation ou non du
         // proxy.
         useProxy = Boolean.valueOf(result);
      } else {
         result = useProxy.booleanValue();
      }

      return result;
   }

   /**
    * Permet de parser la valeur du proxy et de récupérer les valeurs des
    * différents champs du proxy.
    *
    * @param proxy
    *           La valeur du proxy.
    */
   private void parseProxy(final String proxy) {
      // Recherche du caractère de séparation des groupes.
      int index = proxy.indexOf(PROXY_GROUP_SEPARATOR);

      // Deuxième partie du proxy.
      String hostAndPort = null;

      // Si il y a bien un champ "user:password"
      if (index != -1) {
         String usernameAndPassword = proxy.substring(0, index);
         int separation = hostAndPort.indexOf(PROXY_FIELD_SEPARATOR);
         proxyUsername = usernameAndPassword.substring(0, separation);
         proxyPassword = usernameAndPassword.substring(separation + 1);
         hostAndPort = proxy.substring(index);
      } else {
         hostAndPort = proxy;
      }

      // On parse host:port
      int separation = hostAndPort.indexOf(PROXY_FIELD_SEPARATOR);
      proxyHost = hostAndPort.substring(0, separation);
      proxyPort = hostAndPort.substring(separation + 1);
   }

   /**
    * Renvoie le host du proxy.
    *
    * @return Le host du proxy ou null si il n'y a pas de proxy.
    */
   public String getProxyHost() {
      return proxyHost;
   }

   /**
    * Renvoie le port utilisé par le proxy.
    *
    * @return Le port utilisé par le proxy ou null si il n'y a pas de proxy.
    */
   public String getProxyPort() {
      return proxyPort;
   }

   /**
    * Renvoie le mot de passe à utiliser pour la connexion au proxy.
    *
    * @return Le mot de passe à utiliser pour la connexion au proxy.
    */
   public String getProxyPassword() {
      return proxyPassword;
   }

   /**
    * Renvoie le nom d'utilisateur à utiliser pour la connexion au proxy.
    *
    * @return Le nom d'utilisateur à utiliser pour la connexion au proxy.
    */
   public String getProxyUsername() {
      return proxyUsername;
   }
}
