package tk.baumi.szene1;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DownloadGUI extends JFrame {
	private JTextField tfAlbumUrl, tfFilePrefix, tfExportLocation;
	private JButton btExportLocation, startButton, cancelButton;
	private JTextArea dialogArea;
	private JProgressBar progressBar;
	private UpdateListener updateListener;

	public DownloadGUI() {
		super("Szene 1 Downloader");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				dispose();
			}
		});
		setLayout(new BorderLayout());

		JPanel inputPanel = new JPanel(new GridLayout(3, 1));

		tfAlbumUrl = new JTextField("https://");
		JPanel pAlbumUrl = new JPanel(new BorderLayout());
		pAlbumUrl.add(new JLabel("Album URL"), BorderLayout.WEST);
		pAlbumUrl.add(tfAlbumUrl, BorderLayout.CENTER);

		tfFilePrefix = new JTextField();
		JPanel pFilePrefix = new JPanel(new BorderLayout());
		pFilePrefix.add(new JLabel("File Prefix"), BorderLayout.WEST);
		pFilePrefix.add(tfFilePrefix, BorderLayout.CENTER);

		tfExportLocation = new JTextField();
		btExportLocation = new JButton("...");
		JPanel pExportLocation = new JPanel(new BorderLayout());
		pExportLocation.add(new JLabel("Export Location"), BorderLayout.WEST);
		pExportLocation.add(tfExportLocation, BorderLayout.CENTER);
		pExportLocation.add(btExportLocation, BorderLayout.EAST);

		btExportLocation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(tfExportLocation.getText());
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fileChooser.showOpenDialog(DownloadGUI.this) == JFileChooser.APPROVE_OPTION) {
					tfExportLocation.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		inputPanel.add(pAlbumUrl);
		inputPanel.add(pFilePrefix);
		inputPanel.add(pExportLocation);

		add(inputPanel, BorderLayout.NORTH);

		dialogArea = new JTextArea();
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);

		JPanel centreArea = new JPanel(new BorderLayout());
		JScrollPane spDialogArea = new JScrollPane(dialogArea);
		centreArea.add(spDialogArea, BorderLayout.CENTER);
		centreArea.add(progressBar, BorderLayout.SOUTH);

		add(centreArea, BorderLayout.CENTER);

		startButton = new JButton("Start");
		cancelButton = new JButton("Cancel");
		cancelButton.setEnabled(false);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(startButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);

		updateListener = new UpdateListener() {

			@Override
			public void progress(int current, int amount) {
				if(current < 0) {
					startButton.setEnabled(true);
				} else {
					progressBar.setMaximum(amount);
					progressBar.setValue(current);
				}
			}

			@Override
			public void dialog(String text) {
				dialogArea.append(text + "\n");
			}
		};

		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ProgressManager pm = new ProgressManager(tfAlbumUrl.getText(), tfExportLocation.getText(),
						tfFilePrefix.getText(), updateListener);
				Thread thr = new Thread(pm);
				thr.start();
				startButton.setEnabled(false);
			}
		});
	}

}
