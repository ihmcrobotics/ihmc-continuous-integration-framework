package us.ihmc.continuousIntegration.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import us.ihmc.log.LogTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.WriteOption;
import us.ihmc.commons.thread.ThreadTools;

public class SecurityTools
{
   public static void storeLoginInfo(String simpleName, String username, String password)
   {
      storeLoginInfo(Paths.get(System.getProperty("user.home"), simpleName + ".properties"), username, password);
   }
   
   public static void storeLoginInfo(Path path, String username, String password)
   {
      try
      {
         Properties properties = new Properties();
         properties.setProperty(username, password);
         properties.store(FileTools.newPrintWriter(path, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE), null);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public static LoginInfo showLoginDialog()
   {
      if (GraphicsEnvironment.isHeadless())
      {
         System.out.print("Enter a username: ");
         String username = System.console().readLine();
         System.out.print("Enter a password: ");
         char[] password = System.console().readPassword();

         return new LoginInfo(username, new String(password));
      }
      else
      {
         final JDialog dialog = new JDialog();
         JPanel panel = new JPanel();
         panel.add(new JLabel("Username"));
         JTextField usernameTextField = new JTextField(15);
         panel.add(usernameTextField);
         panel.add(new JLabel("Password"));
         JPasswordField passwordTextField = new JPasswordField(15);
         panel.add(passwordTextField);
         dialog.add(panel);
         dialog.setTitle("Enter Login And Press Enter");
         dialog.pack();
         dialog.setLocationRelativeTo(null);
         dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
         passwordTextField.addKeyListener(new KeyAdapter()
         {
            @Override
            public void keyPressed(KeyEvent e)
            {
               if (e.getKeyCode() == KeyEvent.VK_ENTER)
               {
                  dialog.setVisible(false);
               }
            }
         });
         dialog.setVisible(true);

         final boolean[] open = new boolean[1];
         open[0] = true;

         dialog.addWindowListener(new WindowAdapter()
         {
            @Override
            public void windowDeactivated(WindowEvent e)
            {
               open[0] = false;
            }
         });

         while (open[0] == true)
         {
            ThreadTools.sleepSeconds(0.1);
         }

         return new LoginInfo(usernameTextField.getText(), new String(passwordTextField.getPassword()));
      }
   }
   
   public static LoginInfo loadLoginInfo(String simpleName)
   {
      return loadLoginInfo(Paths.get(System.getProperty("user.home"), simpleName + ".properties"));
   }

   public static LoginInfo loadLoginInfo(Path loginInfoPath)
   {
      try
      {
         Properties properties = new Properties();

         properties.load(Files.newInputStream(loginInfoPath));

         return new LoginInfo(properties.keySet().toArray(new String[0])[0], properties.values().toArray(new String[0])[0]);
      }
      catch (IOException e)
      {

         LogTools.info("No login found at " + loginInfoPath);

         LoginInfo loginInfo = showLoginDialog();

         storeLoginInfo(loginInfoPath, loginInfo.getUsername(), loginInfo.getPassword());

         return loginInfo;

      }
   }
}
