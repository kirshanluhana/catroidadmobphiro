/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2017 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.test.content;

import android.test.InstrumentationTestCase;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.FileChecksumContainer;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.test.R;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileChecksumContainerTest extends InstrumentationTestCase {

	private static final int IMAGE_FILE_ID = R.raw.icon;
	private StorageHandler storageHandler;
	private ProjectManager projectManager;
	private File testImage;
	private File testSound;
	private String currentProjectName = "testCopyFile2";
	private String currentSceneName;

	public FileChecksumContainerTest() throws IOException {
	}

	@Override
	protected void setUp() throws Exception {

		TestUtils.clearProject(currentProjectName);
		storageHandler = StorageHandler.getInstance();
		Project testCopyFile = new Project(null, currentProjectName);
		currentSceneName = testCopyFile.getDefaultScene().getName();
		testCopyFile.getXmlHeader().virtualScreenHeight = 1000;
		testCopyFile.getXmlHeader().virtualScreenWidth = 1000;
		projectManager = ProjectManager.getInstance();
		storageHandler.saveProject(testCopyFile);
		projectManager.setProject(testCopyFile);

		final String imagePath = Constants.DEFAULT_ROOT + "/testImage.png";
		testImage = new File(imagePath);
		if (!testImage.exists()) {
			testImage.createNewFile();
		}
		InputStream in = getInstrumentation().getContext().getResources().openRawResource(IMAGE_FILE_ID);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(testImage), Constants.BUFFER_8K);

		byte[] buffer = new byte[Constants.BUFFER_8K];
		int length = 0;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}

		in.close();
		out.flush();
		out.close();

		final String soundPath = Constants.DEFAULT_ROOT + "/testsound.mp3";
		testSound = new File(soundPath);
		if (!testSound.exists()) {
			testSound.createNewFile();
		}
		in = getInstrumentation().getContext().getResources().openRawResource(R.raw.testsound);
		out = new BufferedOutputStream(new FileOutputStream(testSound), Constants.BUFFER_8K);
		buffer = new byte[Constants.BUFFER_8K];
		length = 0;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}

		in.close();
		out.flush();
		out.close();
	}

	@Override
	protected void tearDown() throws Exception {
		TestUtils.clearProject(currentProjectName);
		if (testImage != null && testImage.exists()) {
			testImage.delete();
		}
		if (testSound != null && testSound.exists()) {
			testSound.delete();
		}
		super.tearDown();
	}

	public void testContainer() throws IOException, InterruptedException {

		storageHandler.copyImage(currentProjectName, currentSceneName, testImage.getAbsolutePath(), null);

		String checksumImage = Utils.md5Checksum(testImage);

		FileChecksumContainer fileChecksumContainer = projectManager.getFileChecksumContainer();
		assertTrue("Checksum isn't in container", fileChecksumContainer.containsChecksum(checksumImage));

		//wait to get a different timestamp on next file
		Thread.sleep(2000);

		File newTestImage = storageHandler.copyImage(currentProjectName, currentSceneName, testImage.getAbsolutePath(), null);
		File imageDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProjectName + "/" + currentSceneName + "/"
				+ Constants.IMAGE_DIRECTORY + "/");
		File[] filesImage = imageDirectory.listFiles();

		//nomedia file is also in images folder
		assertEquals("Wrong amount of files in folder", 2, filesImage.length);

		File newTestSound = storageHandler.copySoundFile(testSound.getAbsolutePath());
		String checksumSound = Utils.md5Checksum(testSound);
		assertTrue("Checksum isn't in container", fileChecksumContainer.containsChecksum(checksumSound));
		File soundDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProjectName + "/" + currentSceneName + "/"
				+ Constants.SOUND_DIRECTORY);
		File[] filesSound = soundDirectory.listFiles();

		//nomedia file is also in sounds folder
		assertEquals("Wrong amount of files in folder", 2, filesSound.length);

		fileChecksumContainer.decrementUsage(newTestImage.getAbsolutePath());
		assertTrue("Checksum was deleted", fileChecksumContainer.containsChecksum(checksumImage));
		fileChecksumContainer.decrementUsage(newTestImage.getAbsolutePath());
		assertFalse("Checksum wasn't deleted", fileChecksumContainer.containsChecksum(checksumImage));
		fileChecksumContainer.decrementUsage(newTestSound.getAbsolutePath());
		assertFalse("Checksum wasn't deleted", fileChecksumContainer.containsChecksum(checksumSound));
	}

	public void testDeleteFile() throws IOException, InterruptedException {
		File newTestImage1 = storageHandler.copyImage(currentProjectName, currentSceneName, testImage.getAbsolutePath(), null);
		//wait to get a different timestamp on next file
		Thread.sleep(2000);

		storageHandler.deleteFile(newTestImage1.getAbsolutePath(), false);
		File imageDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProjectName + "/" + currentSceneName + "/"
				+ Constants.IMAGE_DIRECTORY);
		File[] filesImage = imageDirectory.listFiles();
		assertEquals("Wrong amount of files in folder", 1, filesImage.length);

		File newTestSound = storageHandler.copySoundFile(testSound.getAbsolutePath());
		storageHandler.deleteFile(newTestSound.getAbsolutePath(), false);

		File soundDirectory = new File(Constants.DEFAULT_ROOT + "/" + currentProjectName + "/" + currentSceneName + "/"
				+ Constants.SOUND_DIRECTORY);
		File[] filesSound = soundDirectory.listFiles();

		assertEquals("Wrong amount of files in folder", 1, filesSound.length);
	}
}
