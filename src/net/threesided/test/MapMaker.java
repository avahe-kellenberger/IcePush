package net.threesided.test;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MapMaker extends JFrame implements ActionListener {
	private ClickLabel[][][] map;
	private JPanel levelPanels[], mapPanel;
	private JTextField filenameBox, widthBox, heightBox, levelBox;
	private JButton loadButton, saveButton, newButton;
	private JLabel heightLabel;
	private int levW, levH, mapNumLevs, curLev;
	
	public MapMaker() {
		super("mkmap2");
		filenameBox = new JTextField("Filename");
		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
		loadButton.addActionListener(this);
		saveButton.addActionListener(this);
		
		widthBox = new JTextField("Width", 6);
		heightBox = new JTextField("Height", 6);
		levelBox = new JTextField("# Height Planes", 10);
		newButton = new JButton("New");
		newButton.addActionListener(this);
		
		levelPanels = new JPanel[1];
		levelPanels[0] = new JPanel(new GridLayout());
		
		JPanel top = new JPanel(new BorderLayout());
		top.setBorder(BorderFactory.createTitledBorder("Load/Save"));
		top.add(filenameBox, BorderLayout.CENTER);
		
		JPanel opensave = new JPanel();
		opensave.add(loadButton);
		opensave.add(saveButton);
		top.add(opensave, BorderLayout.EAST);
		
		JPanel bottom = new JPanel();
		bottom.setBorder(BorderFactory.createTitledBorder("Create Map"));
		bottom.add(widthBox);
		bottom.add(heightBox);
		bottom.add(levelBox);
		bottom.add(newButton);
		
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(top, BorderLayout.NORTH);
		JPanel cardContainer = new JPanel(new BorderLayout());
		mapPanel = new JPanel(new CardLayout());
		for(int k = 0; k < levelPanels.length; k++) {
			mapPanel.add(levelPanels[k], "level " + k);
		}
		cardContainer.add(mapPanel, BorderLayout.CENTER);
		JPanel nextPrev = new JPanel();
		JButton next = new JButton(">>"), prev = new JButton("<<");
		heightLabel = new JLabel("Height level: " + curLev);
		next.addActionListener(this);
		prev.addActionListener(this);
		nextPrev.add(prev);
		nextPrev.add(next);
		nextPrev.add(heightLabel);
		cardContainer.add(nextPrev, BorderLayout.SOUTH);
		pane.add(cardContainer, BorderLayout.CENTER);
		pane.add(bottom, BorderLayout.SOUTH);
		
		add(pane);
		setSize(640, 480);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("New")) {
			try {
				levW = Integer.parseInt(widthBox.getText());
				levH = Integer.parseInt(heightBox.getText());
				mapNumLevs = Integer.parseInt(levelBox.getText());
				
				map = new ClickLabel[mapNumLevs][levH][levW];
				mapPanel.removeAll();
				levelPanels = new JPanel[mapNumLevs];
				
				for(int h = 0; h < mapNumLevs; h++) {
					levelPanels[h] = new JPanel(new GridLayout(levW, levH));
					for(int y = 0; y < levH; y++) {
						for(int x = 0; x < levW; x++) {
							ClickLabel c = new ClickLabel(0);
							levelPanels[h].add(c);
							map[h][y][x] = c;
						}
					}
					mapPanel.add(levelPanels[h], "level " + h);
				};
				
				pack();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Enter a number for width and height!");
			}
		} else if(cmd.equals("Load")) {
			try {
				load(filenameBox.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.toString());
			}
		} else if(cmd.equals("Save")) {
			try {
				save(filenameBox.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.toString());
			}
		} else if(cmd.equals(">>")) {
			((CardLayout) mapPanel.getLayout()).next(mapPanel);
			updateLevelLabelText();
		} else if(cmd.equals("<<")) {
			((CardLayout) mapPanel.getLayout()).previous(mapPanel);
			updateLevelLabelText();
		}
	}
	
	private void updateLevelLabelText() {
		for(int k = 0; k < mapPanel.getComponents().length; k++) { 
			Component c = mapPanel.getComponents()[k];
			if(c.isVisible())
				heightLabel.setText("Height level: " + k);
		}
	}
	
	public void load(String filename) throws Exception {
		DataInputStream dis = new DataInputStream(new FileInputStream(filename));
		mapNumLevs = dis.readByte();
		levW = dis.readByte();
		levH = dis.readByte();
		
		map = new ClickLabel[mapNumLevs][levH][levW];
		mapPanel.removeAll();
		levelPanels = new JPanel[mapNumLevs];
		
		for(int h = 0; h < mapNumLevs; h++) {
			levelPanels[h] = new JPanel(new GridLayout(levW, levH));
			for(int y = 0; y < levH; y++) {
				for(int x = 0; x < levW; x++) {
					ClickLabel c = new ClickLabel(dis.readByte());
					levelPanels[h].add(c);
					map[h][y][x] = c;
				}
			}
			mapPanel.add(levelPanels[h], "level " + h);
		};
		
		pack();
		dis.close();
	}
	
	public void save(String filename) throws Exception {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(
				filename));
		dos.writeByte(mapNumLevs);
		dos.writeByte(levW);
		dos.writeByte(levH);
		for(int h = 0; h < mapNumLevs; h++) {
			for(int y = 0; y < levH; y++) {
				for(int x = 0; x < levW; x++) {
					dos.writeByte(map[h][y][x].flag);
				}
			}
		};
		dos.flush();
		dos.close();
		System.out.println("Map written.");
	}
	
	public static void main(String[] args) {
		new MapMaker();
	}
	
	class ClickLabel extends JLabel implements MouseListener {
		private int flag;
		
		public ClickLabel(int f) {
			flag = f;
			updateBg();
			setOpaque(true);
			addMouseListener(this);
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(16, 16);
		}
		
		private void updateBg() {
			if(flag == 3)
				setBackground(Color.red);
			else if(flag == 2)
				setBackground(Color.green);
			else if(flag == 1)
				setBackground(Color.black);
			else if(flag == 0)
				setBackground(Color.white);
			else
				setBackground(Color.blue);
		}

		public void mousePressed(MouseEvent e) {
			flag++;
			if(flag > 7)
				flag = 0;
			updateBg();
		}

		public void mouseClicked(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) { }
	}
}
