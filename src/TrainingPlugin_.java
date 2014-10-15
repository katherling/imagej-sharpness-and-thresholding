import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;


public class TrainingPlugin_ implements PlugInFilter {

	private ImagePlus imp;
	
	@Override
	public void run(ImageProcessor ip) {
		
		//int[] kernel = new int[9];
		/*int[] kernel = new int[]
		                       {0, 1, 0,
								1, -4, 1,
								0, 1, 0};
		                       
		ip.convolve3x3(kernel);*/
		//ip.invert();
		//border(ip);
		
		TrainingInterface ti = new TrainingInterface(imp);
		ti.setVisible(true);
		
		
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		
		return DOES_RGB;
	}
	
	public void border (ImageProcessor ip){
		ColorProcessor cp = (ColorProcessor) ip;
		ByteProcessor red = cp.getChannel(1, null);
		ByteProcessor green = cp.getChannel(2, null);
		ByteProcessor blue = cp.getChannel(3, null);
		int countRed = 0;
		int countGreen = 0;
		int countBlue = 0;
		float percentRed = 0;
		float percentGreen = 0;
		float percentBlue = 0;
		for (int i=0; i<ip.getWidth(); i++){
			for (int j=0; j<ip.getHeight(); j++){
				if (red.get(i, j)>=200) {red.set(i, j, 255); countRed++;}
				else  red.set(i, j, 0);
				if (green.get(i, j)>=200) {green.set(i, j, 255); countGreen++;}
				else green.set(i, j, 0);
				if (blue.get(i, j)>=200) {blue.set(i, j, 255); countBlue++;}
				else blue.set(i, j, 0);
			}		
		}
		new ImagePlus("Red", red).show();
		new ImagePlus("Green", green).show();
		new ImagePlus("Blue", blue).show();
		int size = ip.getHeight()*ip.getWidth();
		if (countRed!=0) percentRed =  100*countRed/size;
		if (countGreen!=0) percentGreen =  100*countGreen/size;
		if (countBlue!=0) percentBlue =  100*countBlue/size;
		
		IJ.showMessage("Procent contains", "Red: "+percentRed+"%\nGreen: "+percentGreen+"%\nBlue: "+percentBlue+"%");
		
		
		
		
		
		//cp.setChannel(1, red);
		//cp.setChannel(2, green);
		//cp.setChannel(3, blue);
	}

}
