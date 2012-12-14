package org.jas.commun;

/**
 * Classe permettant d'encasupler les exceptions pouvant être levées.
 */
public final class JasException extends Exception {
   /**
    * serialVersionUID de la classe.
    */
   private static final long serialVersionUID = 1L;

   /**
    * Constructeur par défaut.
    */
   public JasException() {
      super();
   }

   /**
    * Constructeur par exception.
    *
    * @param cause
    *           La cause ayant levée l'exception.
    */
   public JasException(final Throwable cause) {
      super(cause);
   }
}
