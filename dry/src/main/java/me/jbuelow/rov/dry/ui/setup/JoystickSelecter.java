package me.jbuelow.rov.dry.ui.setup;

import me.jbuelow.rov.dry.ui.CloseApplicationConfirmation;
import me.jbuelow.rov.dry.ui.error.ErrorIcon;
import me.jbuelow.rov.dry.ui.error.GeneralError;
import net.java.games.input.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a nice clean graphical method of selecting the primary and secondary joysticks
 */
public class JoystickSelecter extends JDialog {
  private static final long serialVersionUID = 3880297608880881195L;

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JComboBox<Controller> comboBox1;
  private JComboBox<Controller> comboBox2;

  public JoystickSelecter(List<Controller> controllers, Object[] def) {
    setContentPane(contentPane);
    setModal(true);
    setResizable(false);
    setTitle("Joystick Selection");
    getRootPane().setDefaultButton(buttonOK);

    for (Controller controller : controllers) {
      comboBox1.addItem(controller);
      comboBox2.addItem(controller);
    }

    comboBox1.setSelectedItem(def[0]);
    comboBox2.setSelectedItem(def[1]);

    UIManager.put("Button.select",new Color(49, 49, 97));
    UIManager.put("ComboBox.selectionBackground", new Color(84, 84, 84));
    buttonOK.updateUI();
    buttonCancel.updateUI();
    comboBox1.updateUI();
    comboBox2.updateUI();

    buttonOK.setBorder(BorderFactory.createEmptyBorder(5,25,5,25));
    buttonCancel.setBorder(BorderFactory.createEmptyBorder(5,25,5,25));
    comboBox1.setBorder(BorderFactory.createBevelBorder(0));
    comboBox2.setBorder(BorderFactory.createBevelBorder(0));

    buttonOK.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JoystickSelecter.this.onOK();
      }
    });

    buttonCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JoystickSelecter.this.onCancel();
      }
    });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane
        .registerKeyboardAction(new ActionListener() {
                                  @Override
                                  public void actionPerformed(ActionEvent e) {
                                    JoystickSelecter.this.onCancel();
                                  }
                                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    pack();
    setLocationRelativeTo(null);
    setVisible(true);
    toFront();
    requestFocus();
  }

  private void onOK() {
    for (Controller c : getSelection()) {
      if (!c.poll()) {
        GeneralError.display(
            "<html><b>Could connect to controller.</b><br>Controller: " + c.getName()
                + "<br>Please try again.</html>",
            ErrorIcon.JOY_ERROR);
        return;
      }
    }
    dispose();
  }

  private void onCancel() {
    // add your code here if necessary
    CloseApplicationConfirmation.requestExit();
  }

  public Controller[] getSelection() {
    return new Controller[]{(Controller) comboBox1.getSelectedItem(),
        (Controller) comboBox2.getSelectedItem()};
  }

  public static void main(String[] args) {
    List<Controller> list = new ArrayList<>();
    Object[] defs = {"Controller 1", "Controller 2"};
    JoystickSelecter sel = new JoystickSelecter(list, defs);
  }

}
