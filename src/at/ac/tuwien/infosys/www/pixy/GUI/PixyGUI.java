package at.ac.tuwien.infosys.www.pixy.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Checkbox;
import java.awt.Font;
import javax.swing.JButton;

import at.ac.tuwien.infosys.www.pixy.Checker;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.Color;
import java.awt.Toolkit;

@SuppressWarnings("serial")
public class PixyGUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtFilePath;
	public String FileName;
	public boolean VisualParseTree = false;
	public boolean visualXSSDGraphs = false;
	public boolean visualSQLIDGraphs = false;
	public boolean visualXPathIDGraphs = false;
	public boolean visualCmdExecDGraphs = false;
	public boolean visualCdEvalDGraphs = false;
	public boolean scanXSS = true;
	public boolean scanSQLI = true;
	public boolean scanXPathI = true;
	public boolean scanCmdExec = true;
	public boolean scanCdEval = true;
	static PixyGUI frame;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new PixyGUI();
					frame.setLocation(100, 100);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public PixyGUI() {
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage("images\\logo.png"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, -70, 583, 483);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmPreferences = new JMenuItem("Preferences");
		mnFile.add(mntmPreferences);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmHelp = new JMenuItem("Help");
		mnHelp.add(mntmHelp);

		JMenuItem menuItem = new JMenuItem("New menu item");
		mnHelp.add(menuItem);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(245, 245, 245));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblFilePath = new JLabel("File Path :");
		lblFilePath.setFont(new Font("Verdana", Font.BOLD, 12));
		lblFilePath.setBounds(10, 104, 121, 17);
		contentPane.add(lblFilePath);

		txtFilePath = new JTextField();
		txtFilePath.setBounds(110, 103, 297, 20);
		contentPane.add(txtFilePath);
		txtFilePath.setColumns(10);

		JLabel lblChooseVulnerablities = new JLabel("Choose Vulnerablities :");
		lblChooseVulnerablities.setFont(new Font("Verdana", Font.BOLD, 12));
		lblChooseVulnerablities.setBounds(10, 137, 191, 17);
		contentPane.add(lblChooseVulnerablities);

		final Checkbox chkXpathScan = new Checkbox("XPath Injection");
		chkXpathScan.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkXpathScan.setState(true);
		chkXpathScan.setBounds(349, 158, 126, 22);
		contentPane.add(chkXpathScan);

		final Checkbox chkXSSScan = new Checkbox("XSS");
		chkXSSScan.setState(true);
		chkXSSScan.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkXSSScan.setBounds(110, 158, 95, 22);
		contentPane.add(chkXSSScan);

		final Checkbox chkSQLIScan = new Checkbox("SQL Injection");
		chkSQLIScan.setState(true);
		chkSQLIScan.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkSQLIScan.setBounds(211, 158, 95, 22);
		contentPane.add(chkSQLIScan);

		final Checkbox chkCmdExecIScan = new Checkbox("Command Execution");
		chkCmdExecIScan.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkCmdExecIScan.setState(true);
		chkCmdExecIScan.setBounds(110, 186, 134, 22);
		contentPane.add(chkCmdExecIScan);

		final Checkbox chkCdEvalScan = new Checkbox("Code Evaluation");
		chkCdEvalScan.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkCdEvalScan.setState(true);
		chkCdEvalScan.setBounds(349, 186, 126, 22);
		contentPane.add(chkCdEvalScan);

		JLabel lblVisualizationOptions = new JLabel("Visualization Options :");
		lblVisualizationOptions.setFont(new Font("Verdana", Font.BOLD, 12));
		lblVisualizationOptions.setBounds(10, 236, 191, 17);
		contentPane.add(lblVisualizationOptions);

		final Checkbox chkParseTreeVisual = new Checkbox("Parse Tree");
		chkParseTreeVisual.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkParseTreeVisual.setBounds(110, 271, 95, 22);
		contentPane.add(chkParseTreeVisual);

		final Checkbox chkCmdExecDGraphs = new Checkbox("Command Execution D.Graphs");
		chkCmdExecDGraphs.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkCmdExecDGraphs.setBounds(318, 299, 206, 22);
		contentPane.add(chkCmdExecDGraphs);

		final Checkbox chkXPathDGraphs = new Checkbox("XPath Injection D.Graphs");
		chkXPathDGraphs.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkXPathDGraphs.setBounds(110, 299, 153, 22);
		contentPane.add(chkXPathDGraphs);

		final Checkbox chkSQLIDGraphs = new Checkbox("SQL Injection D.Graphs");
		chkSQLIDGraphs.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkSQLIDGraphs.setBounds(349, 271, 148, 22);
		contentPane.add(chkSQLIDGraphs);

		final Checkbox chkCdEvalDGraphs = new Checkbox("Code Evaluation D.Graphs");
		chkCdEvalDGraphs.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkCdEvalDGraphs.setBounds(110, 327, 184, 22);
		contentPane.add(chkCdEvalDGraphs);

		final Checkbox chkXSSDGraphs = new Checkbox("XSS D.Graphs");
		chkXSSDGraphs.setFont(new Font("Verdana", Font.PLAIN, 12));
		chkXSSDGraphs.setBounds(211, 271, 95, 22);
		contentPane.add(chkXSSDGraphs);

		JButton btnScan = new JButton("Scan");
		btnScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				FileName = txtFilePath.getText();
				scanXSS = chkXSSScan.getState();
				scanSQLI = chkSQLIScan.getState();
				scanXPathI = chkXpathScan.getState();
				scanCmdExec = chkCmdExecIScan.getState();
				scanCdEval = chkCdEvalScan.getState();

				VisualParseTree = chkParseTreeVisual.getState();
				visualXSSDGraphs = chkXSSDGraphs.getState();
				visualSQLIDGraphs = chkSQLIDGraphs.getState();
				visualXPathIDGraphs = chkXPathDGraphs.getState();
				visualCmdExecDGraphs = chkCmdExecDGraphs.getState();
				visualCdEvalDGraphs = chkCdEvalDGraphs.getState();

				Checker.frame = frame;
				Checker.Scan();
			}
		});
		btnScan.setBounds(226, 374, 89, 23);
		contentPane.add(btnScan);

		Panel panel = new Panel();
		panel.setBackground(SystemColor.activeCaption);
		panel.setBounds(0, 0, 580, 74);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("PixyOO");
		lblNewLabel.setFont(new Font("Showcard Gothic", Font.PLAIN, 35));
		lblNewLabel.setBounds(10, 5, 166, 71);
		panel.add(lblNewLabel);

		JLabel lblStaticCodeAnalysis = new JLabel("Static Code Analysis Tool");
		lblStaticCodeAnalysis.setFont(new Font("Papyrus", Font.PLAIN, 16));
		lblStaticCodeAnalysis.setBounds(161, 43, 205, 27);
		panel.add(lblStaticCodeAnalysis);

	}
}
