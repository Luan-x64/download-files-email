package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;


public class main {
	private static Store store;
	private static int countMessages;
	private static int countMessagesOld;
	private static String saveDirectory;
	private static Message[] arrayMessages;
	private static Folder folderInbox;
	private static boolean verf = true;
	

	private void downloadFile() {
		
	}
	public void setSaveDirectory(String dir) {
		main.saveDirectory = dir;
	}
	private static void desconnectAccount() {
					try {
						folderInbox.close(false);
						store.close();
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
	}
	private static void connectAccount(String host, String port, String user, String pass) {
		Properties properties = new Properties();
		properties.put("mail.pop3.host", host);
		properties.setProperty("mail.pop3.socketFactory.fallback", "false");
		Session session = Session.getDefaultInstance(properties);
		try {
			store = session.getStore("pop3");
			store.connect(user, pass);
			// opens the inbox folder
			folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_ONLY);
			countMessagesOld = folderInbox.getMessageCount(); //aqui pega o da primeira conexao
			
			
			// captura mensagem do servidor
			arrayMessages = folderInbox.getMessages();
			// disconnect
			//folderInbox.close(false);
			//store.close();
		} catch (NoSuchProviderException ex) {
			System.out.println("No provider for pop3.");
			ex.printStackTrace();
			//log.error(ex);
		} catch (MessagingException ex) {
			System.out.println("Could not connect to the message store");
			ex.printStackTrace();
			//log.error(ex);
		}
	}
	
	private static void listEmails() {
		try {
			for (int i = 0; i < arrayMessages.length; i++) {
				Message message = arrayMessages[i];
				Address[] fromAddress = message.getFrom();
				String from = fromAddress[0].toString();
				int lengFrom = from.length();
				int indexFrom = from.indexOf("<");
				String subject = message.getSubject();
				String sentDate = message.getSentDate().toString();
				
				
				System.out.println("----------------------------------");
				System.out.println("REMETENTEee:"+ from);
				
				System.out.println(sentDate);
				//System.out.println("REMETENTE:"+ from.substring(indexFrom,lengFrom));
				//System.out.println()
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
							
							String typeFile = part.getContentType();
							 System.out.println(fileName);
							//String md5 = part.saveFile();;
							//System.out.println(md5);
							attachFiles += fileName + ", ";
							if(part.getFileName().contains(".pdf")){
								int fileNam12e = fileName.indexOf(".pdf");
								String nameFile = fileName.substring(0, fileNam12e);
								 File temp = File.createTempFile(nameFile, ".pdf" );
					            System.out.println("Temp file: " + temp.getAbsolutePath());
					            part.saveFile(temp.getAbsolutePath());
					            
								if(verifFile(temp.getAbsolutePath(),saveDirectory + File.separator + fileName)) {
									System.out.println("existo");
									temp.delete();
								} else {
									System.out.println("nao existo");
									part.saveFile(saveDirectory + File.separator + fileName);
									temp.delete();
								}
								
							}
							
						} else {
							// this part may be the message content
							messageContent = part.getContent().toString();
						}
					}

					if (attachFiles.length() > 1) {
						attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
					}
				} else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
					Object content = message.getContent();
					if (content != null) {
						messageContent = content.toString();
					}
				}
				
				/*print out details of each message
				System.out.println("Message #" + (i + 1) + ":");
				System.out.println("\t From: " + from);
				System.out.println("\t Subject: " + subject);
				System.out.println("\t Sent Date: " + sentDate);
				System.out.println("\t Message: " + messageContent);
				System.out.println("\t Attachments: " + attachFiles);*/
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public static void verifNewEmail() {
		while(verf) {
			try {
			    Thread.sleep(5000);
			    //https://stackoverflow.com/questions/51274502/receive-new-emails-automatically-javamail
				countMessages = folderInbox.getMessageCount();
				if(countMessages > countMessagesOld) {
					System.out.println("Nova mensagem");
				} else {
					System.out.println("Não existe novos emails");
				}
			} catch (MessagingException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//implementar forma que o codigo rode em certas horas ou certo tempo 
		
		
		
		//velho
		if(countMessages > countMessagesOld) {
			System.out.println("Nova mensagem");
		} else {
			System.out.println("Não existe novos emails");
		}
	}
	
	private static boolean verifFile(String file1, String file2) {
		File f1 = new File(file1);
        File f2 = new File(file2);
        byte[] f1_buf = new byte[1048576];
        byte[] f2_buf = new byte[1048576];
        int len;
        if (f1.length() == f2.length()) {
            try {
                InputStream isf1 = new FileInputStream(f1);
                InputStream isf2 = new FileInputStream(f2);
                try {
                    while (isf1.read(f1_buf) >= 0) {
                        len=isf2.read(f2_buf);
                        for (int j = 0; j < len; j++) {
                            if (f1_buf[j] != f2_buf[j]) {
                                return false; // tamanho igual e conteudo diferente
                            }
                        }
                    }
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e) {
            }
        } else {
            return false; // tamanho e conteudo diferente
       }
        return true; // arquivos iguais
	}
	
	
	
	
	public static void main(String[] args) {
		String host = "mail.ewq13sr.org";
		String port = "110";
		String userName = "hiewqew@rfds.net"; //username for the mail you want to read
		String password = "1234@"; //password
		//pasta para salvar
		String saveDirectory = "\\folder\\";

		main receiver = new main();
		receiver.setSaveDirectory(saveDirectory);
		main.connectAccount(host, port, userName, password);
		main.listEmails();
		verifNewEmail();
		main.desconnectAccount();
		

	}
	
}
