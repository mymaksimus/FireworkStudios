package de.skysoldier.fireworkstudiu.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class StudioMainGui {
	
	public StudioMainGui(){
		JFrame f = new JFrame();
		f.setSize(800, 600);
		f.setLocationRelativeTo(null);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel content = new JPanel(new BorderLayout());
		
		JMenuBar bar = new JMenuBar();
		bar.setPreferredSize(new Dimension(0, 20));
		JMenu menu = new JMenu("test..");
		JMenuItem item = new JMenuItem("hallo! :D");
		menu.add(item);
		JMenu subMenu = new JMenu("sub menu??");
		JMenuItem subItem = new JMenuItem("sub item iteeeem1!");
//		subItem.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e){
//				JOptionPane.showMessageDialog(null, "are ye fuck'n kiddin' me?");
//			}
//		});
		subMenu.add(subItem);
		menu.addSeparator();
		menu.add(subMenu);
		bar.add(menu);
		content.add(bar, BorderLayout.NORTH);
		
		f.add(content);
		f.setVisible(true);
	}
	
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new StudioMainGui();
	}
}
