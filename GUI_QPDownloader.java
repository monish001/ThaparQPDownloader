/*
 * GUI_QPDownloader.java
 *
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.AbstractButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.Vector;
import java.util.ArrayList;

class CheckBoxRenderer implements TableCellRenderer, ItemListener {
    public CheckBoxRenderer() {
    }
	
      public void itemStateChanged(ItemEvent itemEvent) {
        AbstractButton abstractButton = (AbstractButton)itemEvent.getSource();
        int state = itemEvent.getStateChange();
        if (state == ItemEvent.SELECTED) {
			//cb.setSelected(!cb.isSelected());
			System.out.println(course + " checkbox change");
        }
	  }
    
	public Component getTableCellRendererComponent(
                            JTable table, Object ob,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
		cb = (JCheckBox)ob;
		course = cb.getText();
        if(cb.getText().equals("")){//if label is ""
			cb.setEnabled(false);
		}
		//cb.addItemListener(this);
        return cb;
    }
	JCheckBox cb;
	String course;
}

public class GUI_QPDownloader extends JFrame implements ActionListener{
    JButton dl_btn = new JButton("Download Selected");
	JTable table;
	QPDownloader qpdown;
	Object[][] data;
    public GUI_QPDownloader(String name) {
        super(name);
        //setResizable(false);
    }

    public void addLoadingMessage(final Container pane) {
			
            pane.setLayout(new BorderLayout());
			JLabel headerLabel = new JLabel();
            headerLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 16));
            headerLabel.setText(" Please wait. List of question papers is being downloaded. It may take a minute or two.  ");

			headerLabel.setVerticalAlignment(SwingConstants.CENTER);
            pane.add(headerLabel, BorderLayout.CENTER);

/*            // add the image label
            ImageIcon ii = new ImageIcon(this.getClass().getResource(
                    "temp.gif"));
			JLabel imageLabel = new JLabel();
            imageLabel.setIcon(ii);
            pane.add(imageLabel, BorderLayout.CENTER);*/

//			pane.update(pane.getGraphics());
	}
    public void addComponentsToPane(final Container pane) {
		qpdown = new QPDownloader();
		final Object[] columnNames = new String[qpdown.seasonPagesInfo.size()];
		int i=-1;
		for(SeasonPage sp: qpdown.seasonPagesInfo){
			columnNames[++i] = sp.toString();
		}

		System.out.println(SeasonPage.courses.size()+" Question Papers in total for "+qpdown.seasonPagesInfo.size() + " exams.");
		data = new JCheckBox[SeasonPage.courses.size()][qpdown.seasonPagesInfo.size()];
		final boolean[][] editable = new boolean[SeasonPage.courses.size()][qpdown.seasonPagesInfo.size()];
		int r=-1;
		for(String course: SeasonPage.courses){// for each course or row
			int c = -1;	++r;
			//System.out.println(course);
			for(SeasonPage season: qpdown.seasonPagesInfo){//for each season or col
				//if course  exits in season.coursesInfo.name then add JCheckBox
				boolean exists = false;
				for(CourseInfo ci: season.coursesInfo){
					if((ci.name).equals(course)){
						exists = true;
						break;
					}
				}
				if(exists){
					data[r][++c]=new JCheckBox(course, false);
					editable[r][c] = true;
				}else{
					data[r][++c]=new JCheckBox("", false);
					editable[r][c] = false;
				}
			}
		}

//System.out.println((new JCheckBox()).getClass().toString() + " " + data[0][0].getClass().toString());
		final ArrayList<String> v = new ArrayList<String>(SeasonPage.courses);
		table = new JTable(){
			public TableCellRenderer getCellRenderer(int row, int column) {
				return new CheckBoxRenderer();
			}
		};
		table.setModel(new DefaultTableModel(data, columnNames){
			public boolean isCellEditable(int r, int c) {
				return editable[r][c];
			}
			public Class getColumnClass(int columnIndex){
//				System.out.println("In getColumnClass");
				return Boolean.class;//(new JCheckBox()).getClass();
			}
			public Object getValueAt(int row, int col){
//				System.out.println("In getValueAt");
				return data[row][col];
			}
			public void setValueAt(Object value, int row, int col)
			{
//				System.out.println("In setValueAt");
				((JCheckBox)data[row][col]).setSelected(!((JCheckBox)data[row][col]).isSelected());
				fireTableCellUpdated(row, col);
			}
		});
		table.setFillsViewportHeight(true);
		
		//controls
        JPanel controls = new JPanel();
		dl_btn.addActionListener(this);
        controls.add(dl_btn);
		pane.removeAll();
		pane.setLayout(new BorderLayout());
        //pane.add(table.getTableHeader(), BorderLayout.PAGE_START);
        pane.add(new JScrollPane(table), BorderLayout.CENTER);
        pane.add(controls, BorderLayout.SOUTH);
    }

	public void actionPerformed(ActionEvent e){
		//System.out.println("Download btn event");
		JFileChooser chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//
		// disable the "All files" option.
		//
		chooser.setAcceptAllFileFilterUsed(false);
		//  
		String path = "";
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			JOptionPane.showMessageDialog(this, "Press OK to start downloading selected Question Papers");
			path = chooser.getSelectedFile() + "";//  \\ for windows
			if(path.equals("") || path.endsWith("\\") || path.endsWith("/"));
			else if(path.indexOf('/') == -1)
				path += "\\";
			else path += "/";
//			System.out.println("getSelectedFile() : " +  path);

		int r=-1;
		boolean status = false;
		labelOuter:
		for(String course: SeasonPage.courses){//for each course or row
			int c = -1;	++r;
			for(SeasonPage season: qpdown.seasonPagesInfo){//for each season or col
				if(((JCheckBox)data[r][++c]).isSelected()){//if this checkbox is selected
					for(CourseInfo ci : season.coursesInfo){//for each courseInfo
						if(course.equals(ci.name)){//find selected only
							status = FileDownloader.download(new String[]{ci.link, path+course+" "+season.name+".pdf"});
							if(status)
								break;
							else{//show read/write permission error message and break
								System.out.println("Read/write permission error for the chosen directory");
								JOptionPane.showMessageDialog(this, "Read/write permission error for the chosen directory");
								break labelOuter;
							}
						}
					}
				}
			}
		}
		if(status){
			//show dialog box here
			JOptionPane.showMessageDialog(this, "Selected Question Papers Downloaded to "+path);
			System.out.println("Selected Question Papers Downloaded to "+path);
		}
		
		}
		else {
			System.out.println("No Selection ");
		}
		
	}

	public static void main(String[] args){
        /* Use an appropriate Look and Feel */
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
		final GUI_QPDownloader frame = new GUI_QPDownloader("Thapar Question Paper Downloader");
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				//Create and set up the window.
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frame.setJMenuBar((new MyJMenuBar(frame)).menuBar);
				
				//Display the window.
				//frame.pack();

				//Set up the content pane.
				frame.addLoadingMessage(frame.getContentPane());
				frame.pack();
				frame.setSize(new Dimension((int)(frame.getSize().getWidth()), 300));
				frame.setResizable(false);
				frame.setVisible(true);
				frame.update(frame.getGraphics());
            }
        });
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				frame.addComponentsToPane(frame.getContentPane());
				frame.setResizable(true);
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
	}
}
class MyJMenuBar{
	JMenuBar menuBar = new JMenuBar();
	MyJMenuBar(final JFrame frame){
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		JMenuItem menuItemAbout = new JMenuItem("About", KeyEvent.VK_A);
		JMenuItem menuItemExit = new JMenuItem("Exit", KeyEvent.VK_X);
		menuItemAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//show about dialogue box
				JOptionPane.showMessageDialog(frame, 
//"   Programming language: Java\n"+
"   Developer: Monish Gupta\n"+
"   monishgupta.blogspot.com", "About", JOptionPane.PLAIN_MESSAGE);
			}
		});
		menuItemExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		menu.add(menuItemAbout);
		menu.add(menuItemExit);
	}
}