package com.tool.form;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tool.model.XYZVersionInfo;
import com.tool.util.GlobalConstants;
import com.tool.util.ToolUtils;

public class MainForm {

	private JFrame frame;
	private JTextField inputPath;
	private static Map<String, String> xyzPaths = new HashMap<String, String>();

	private static String LAST_USED_FOLDER = "";
	private static String LAST_SAVE_FOLDER = "";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainForm window = new MainForm();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainForm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 625, 255);
		frame.setFocusable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("XYZ projects path:");
		lblNewLabel.setBounds(15, 9, 134, 20);
		frame.getContentPane().add(lblNewLabel);

		JLabel label = new JLabel("");
		label.setBounds(216, 19, 0, 0);
		frame.getContentPane().add(label);

		inputPath = new JTextField();
		inputPath.setBounds(15, 45, 418, 41);
		frame.getContentPane().add(inputPath);
		inputPath.setColumns(10);

		JButton btnNewButton = new JButton("Browse...");
		btnNewButton.setBounds(448, 44, 134, 42);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Preferences prefs = Preferences.userRoot().node(getClass().getName());
				JFileChooser chooser = new JFileChooser(prefs.get(LAST_USED_FOLDER, new File(".").getAbsolutePath()));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(frame);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			       inputPath.setText(chooser.getSelectedFile().getAbsolutePath());
			       prefs.put(LAST_USED_FOLDER, chooser.getSelectedFile().getAbsolutePath());
			    }
			}
		});
		frame.getContentPane().add(btnNewButton);

		JLabel msg = new JLabel("msg");
		msg.setForeground(Color.RED);
		msg.setBounds(15, 91, 567, 20);
		msg.setVisible(false);
		frame.getContentPane().add(msg);

		JButton btnCheckUnmatchedVersion = new JButton("Check unmatched version & export to CSV");
		btnCheckUnmatchedVersion.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String rootPath = inputPath.getText();
				if (rootPath == null || rootPath.isEmpty()) {
					msg.setText("Please select the path of XYZ projects.");
					msg.setVisible(true);
				} else {
					//read xyz_root pom, get version of all
					try {
						Map<String, String> artifactMap = readXYZRootPomFile(rootPath);
						List<XYZVersionInfo> results = getXYZModulePomVersion(artifactMap, rootPath);
						msg.setVisible(false);
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

						Preferences prefs = Preferences.userRoot().node(getClass().getName());
						JFileChooser chooser = new JFileChooser(prefs.get(LAST_SAVE_FOLDER, new File(".").getAbsolutePath()));
						chooser.addChoosableFileFilter(new FileNameExtensionFilter("*.csv", GlobalConstants.CSV_EXT));
						chooser.setAcceptAllFileFilterUsed(false);
						chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);

						int returnVal = chooser.showSaveDialog(frame.getContentPane());
			            if (returnVal == JFileChooser.APPROVE_OPTION) {
			            	String filePath = "";
			            	if (!chooser.getSelectedFile().getName().endsWith(GlobalConstants.CSV_EXT)) {
			            		filePath = chooser.getSelectedFile().getCanonicalPath() + ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0];
			            	} else {
			            		filePath = chooser.getSelectedFile().getAbsolutePath();
			            	}

			            	File file = new File(filePath);
			            	if (file.exists()) {
			            		int response = JOptionPane.showConfirmDialog(null,
			            	            "Do you want to replace the existing file?",
			            	            "Confirm", JOptionPane.YES_NO_OPTION,
			            	            JOptionPane.QUESTION_MESSAGE);
			            	    if (response != JOptionPane.YES_OPTION) {
			            	        return;
			            	    }
			            	}

			            	FileWriter fw = new FileWriter(filePath, false);
			            	String header = "Module,Version in module pom,Version in root pom,Unmatched (Y/N)";
			            	PrintWriter printWriter = new PrintWriter(fw);
			            	printWriter.println(header);
			            	for (XYZVersionInfo xyz: results) {
			            		printWriter.println(xyz.toString());
			            	}
			            	printWriter.close();
			                prefs.put(LAST_SAVE_FOLDER, chooser.getSelectedFile().getParent());
			            }
					} catch (SAXException e1) {
						msg.setText("Can not parse the pom file of xyz_root.");
						msg.setVisible(true);
					} catch (IOException e1) {
						msg.setText("Can not find the pom file of xyz_root.");
						msg.setVisible(true);
					} catch (ParserConfigurationException e1) {
						msg.setText("Can not parse the pom file of xyz_root.");
						msg.setVisible(true);
					}
				}
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		btnCheckUnmatchedVersion.setBounds(15, 127, 418, 41);
		frame.getContentPane().add(btnCheckUnmatchedVersion);

		JLabel label_1 = new JLabel("");
		label_1.setBounds(694, 19, 0, 0);
		frame.getContentPane().add(label_1);
	}

	private Map<String, String> readXYZRootPomFile(String rootPath) throws SAXException, IOException, ParserConfigurationException {
		//map artifactId with its version
		Map<String, String> artifactMap = new HashMap<String, String>();

		String xyzRootPomPath = rootPath + "\\" + GlobalConstants.XYZ_ROOT + "\\" + GlobalConstants.POM;
		File file = new File(xyzRootPomPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		Document doc = builder.parse(file);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName(GlobalConstants.DEPENDENCY_TAGNAME);

        for (int temp = 0; temp < nList.getLength(); temp++) {
        	Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            	Element eElement = (Element) nNode;
            	String artifactId = eElement
                        .getElementsByTagName("artifactId")
                        .item(0)
                        .getTextContent();
            	String version = eElement
                        .getElementsByTagName("version")
                        .item(0)
                        .getTextContent();

            	if (!ToolUtils.isEmpty(artifactId) && artifactId.startsWith(GlobalConstants.XYZ_PREFIX)) {
            		artifactMap.put(artifactId, version);
            	}
            }
        }
        return artifactMap;
	}

	private List<XYZVersionInfo> getXYZModulePomVersion(Map<String, String> artifactMap, String rootPath) {
		List<XYZVersionInfo> xyzs = new ArrayList<XYZVersionInfo>();
		findAllXYZDirectories(new File(rootPath).listFiles());
		//xyzPaths has data now
		for (Entry<String, String> entry: artifactMap.entrySet()) {
			XYZVersionInfo xyz;
			//get version by read pom file
			String pomPath = xyzPaths.get(entry.getKey()) + "\\" + GlobalConstants.POM;
			String version = "";
			String isUnmatched = "";
			try {
				version = getVersion(pomPath);
				isUnmatched = version.equals(entry.getValue()) ? GlobalConstants.NO : GlobalConstants.YES;
			} catch (ParserConfigurationException | SAXException | IOException e) {
				isUnmatched = GlobalConstants.YES;
			}
			xyz = new XYZVersionInfo(entry.getKey(), entry.getValue(), version, isUnmatched);
			xyzs.add(xyz);
		}
		return xyzs;
	}

	private String getVersion(String pomPath) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(pomPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		Document doc = builder.parse(file);
		doc.getDocumentElement().normalize();
		String version = doc
				.getElementsByTagName(GlobalConstants.VERSION_TAGNAME)
                .item(0)
                .getTextContent();
		return version;
	}

	public void findAllXYZDirectories(File[] files) {
		for (File file : files) {
	        if (file.isDirectory()) {
	        	if (file.getName().startsWith(GlobalConstants.XYZ_PREFIX)) {
	        		xyzPaths.put(file.getName(), file.getAbsolutePath());
	        	}
	        	findAllXYZDirectories(file.listFiles());
	        }
	    }
	}
}
