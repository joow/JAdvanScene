package org.jas.commun;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Implémentation de la classe Authenticator avec un mot de passe.
 */
public final class PasswordAuthenticator extends Authenticator {
   /**
    * Nom de l'utilisateur pour la connexion au proxy.
    */
   private transient String username = null;

   /**
    * Mot de passe de l'utilisateur pour la connexion au proxy.
    */
   private transient String password = null;

   /**
    * Renvoie les informations d'authentification.
    *
    * @return PasswordAuthentication Les informations d'authentification.
    */
   protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password.toCharArray());
   }

   /**
    * Modifier le mot de passe du proxy.
    *
    * @param aPassword
    *           Le mot de passe du proxy.
    */
   public void setPassword(final String aPassword) {
      this.password = aPassword;
   }

   /**
    * Modifier le nom d'utilisateur du proxy.
    *
    * @param aUsername
    *           Le nom d'utilisateur du proxy.
    */
   public void setUsername(final String aUsername) {
      this.username = aUsername;
   }
}
