package tk.baumi.szene1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import javax.swing.SwingUtilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProgressManager implements Runnable {

	private String albumUrl, outputPath, filePrefix;
	private UpdateListener updateListener;

	public ProgressManager(String albumUrl, String outputPath, String filePrefix, UpdateListener updateListener) {
		super();
		this.albumUrl = albumUrl;
		this.outputPath = outputPath;
		this.filePrefix = filePrefix;
		this.updateListener = updateListener;
	}

	@Override
	public void run() {
		try {
			Document firstDocument = Jsoup.connect(albumUrl + "1").get();
			Element photo_count = firstDocument.selectFirst(".photo-count");
			int amount = Integer.parseInt(photo_count.html());
			for (int i = 1; i <= amount; i++) {
				Document doc = Jsoup.connect(albumUrl + i).get();
				Elements newsHeadlines = doc.select(".photo");
				String photoUrl = "";
				for (Element headline : newsHeadlines) {
					if (headline.hasAttr("data-photo-url")) {
						photoUrl = headline.attr("data-photo-url");
					}
				}
				if (photoUrl != null) {
					postProgressUpdate(i, amount);
					postDialogUpdate("Downloading file " + photoUrl);
					String outputFile = outputPath + filePrefix + i + ".jpg";
					downloadPicture(photoUrl, outputFile);
					postDialogUpdate("file location: " + outputFile);
				}
			}
			postProgressUpdate(-1, amount);
		} catch (IOException ex) {
			postDialogUpdate(ex.getMessage());
			postProgressUpdate(-1, -1);
		}
	}

	private void downloadPicture(String pictureUrl, String outputFile) {
		ReadableByteChannel readableChannelForHttpResponseBody = null;
		FileChannel fileChannelForDownloadedFile = null;
		FileOutputStream fosForDownloadedFile = null;
		try {
			File outputFileObject = new File(outputFile);
			if (!outputFileObject.exists()) {
				URL robotsUrl = new URL(pictureUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) robotsUrl.openConnection();
				urlConnection.addRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				urlConnection.addRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0");
				urlConnection.addRequestProperty("Accept-Encoding", "gzip, deflate, br");
				urlConnection.addRequestProperty("Host", "www.szene1.at");
				urlConnection.addRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");

				// Get a readable channel from url connection
				readableChannelForHttpResponseBody = Channels.newChannel(urlConnection.getInputStream());

				// Create the file channel to save file
				fosForDownloadedFile = new FileOutputStream(outputFile);
				fileChannelForDownloadedFile = fosForDownloadedFile.getChannel();

				// Save the body of the HTTP response to local file
				fileChannelForDownloadedFile.transferFrom(readableChannelForHttpResponseBody, 0, Long.MAX_VALUE);
			}
		} catch (IOException ioException) {
			postDialogUpdate(ioException.getMessage());
		} finally {

			if (readableChannelForHttpResponseBody != null) {

				try {
					readableChannelForHttpResponseBody.close();
				} catch (IOException ioe) {
					System.out.println("Error while closing response body channel");
				}
			}

			if (fileChannelForDownloadedFile != null) {

				try {
					fileChannelForDownloadedFile.close();
				} catch (IOException ioe) {
					System.out.println("Error while closing file channel for downloaded file");
				}
			}

			if (fosForDownloadedFile != null) {
				try {
					fileChannelForDownloadedFile.close();
				} catch (IOException e) {
					System.out.println("Error while closing file channel for output file");
				}
			}
		}
	}

	private void postDialogUpdate(String text) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateListener.dialog(text);
			}
		});
	}

	private void postProgressUpdate(int progress, int amount) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateListener.progress(progress, amount);
			}
		});
	}
}
