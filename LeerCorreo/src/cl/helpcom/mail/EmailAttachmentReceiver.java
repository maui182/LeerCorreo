package cl.helpcom.mail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import javax.security.auth.Subject;
import javax.swing.JOptionPane;

import com.sun.mail.imap.protocol.MessageSet;


/**
 * @author Mauricio Rodriguez
 *
 */
public class EmailAttachmentReceiver {
    private String saveDirectory;

    /**
     * Sets the directory where attached files will be stored.
     * @param dir absolute path of the directory
     */
    public void setSaveDirectory(String dir) {
        this.saveDirectory = dir;
    }

    /**
     * Guarda los archivos descargados en una carpeta de destino
     * @param host
     * @param port
     * @param userName
     * @param password
     * @param carpetaDestino
     * @param sujeto
     */
    public void downloadEmailAttachments(String host, String port,
        String userName, String password, String carpetaDestino,String sujeto ) {
        
    	Properties properties = new Properties();

        // server setting
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);

        // SSL setting
        properties.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3.socketFactory.fallback", "false");
        properties.setProperty("mail.pop3.socketFactory.port",String.valueOf(port));
        properties.put("mail.imaps.partialfetch", false);
        Session session = Session.getDefaultInstance(properties, null);

        try {
            // connects to the message store
            Store store = session.getStore("pop3");
            store.connect(userName, password);

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_WRITE);
            //folderInbox.open(Folder.READ_ONLY);
            FromStringTerm fromTerm = new FromStringTerm(sujeto);
            SearchTerm searchCondition = new SearchTerm() {
                @Override
                public boolean match(Message message) {
                	
                    try {	
                        if (message.getSubject().contains("") || message.getSubject().contains(null)) {                        	
                            return true;
                        }
                    }catch (Exception e) {
						
					}
                    return false;
                }
            };
            //FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
           // fetches new messages from server
           Message[] arrayMessages = folderInbox.search(searchCondition);

           System.out.println("Existen "+folderInbox.getUnreadMessageCount()+" correos por leer");

           for (int i = 0; i < arrayMessages.length; i++) {
                Message message = arrayMessages[i];
                Address[] fromAddress = message.getFrom();

                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();

                String contentType = message.getContentType();
                String messageContent = "";

                // store attachment file name, separated by comma
                String attachFiles = "";

                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String fileName = part.getFileName();
                            attachFiles += fileName + ", ";
                            part.saveFile(carpetaDestino+fileName.replaceAll("/", ""));
                        } else {
                            // this part may be the message content
                            messageContent = part.getContent().toString();
                        }
                    }

                    if (attachFiles.length() > 1) {
                        attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }
                // print out details of each message
                System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + sentDate);
//              System.out.println("\t Message: " + messageContent);
                System.out.println("\t Attachments: " + attachFiles);
                //Borrar mensajes leídos
                
                Flags deleted = new Flags(Flags.Flag.DELETED);
                folderInbox.setFlags(arrayMessages,deleted, true);
                message.setFlag(Flags.Flag.DELETED, true);
//                
//                boolean expunge = true;
//                folderInbox.close(expunge);
                
            }

            // disconnect
            folderInbox.close(true);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }}

