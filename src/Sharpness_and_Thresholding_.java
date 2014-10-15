import javax.swing.JFrame;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


@SuppressWarnings("serial")
public class Sharpness_and_Thresholding_  implements PlugInFilter {
	private ImagePlus imp;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}

	@Override
	public void run(ImageProcessor ip) {
		MainFrame frame = new MainFrame(ip, imp);
		frame.setVisible(true);
	}
}
