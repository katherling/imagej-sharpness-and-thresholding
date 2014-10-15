import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;


@SuppressWarnings("serial")
public class TrainingInterface extends JFrame {
	private ImageProcessor ip;
	private int[] xCoordinates;
	private int[] yCoordinates;
	private ImageCanvas canvas;
	public TrainingInterface(ImagePlus ip){
		this.ip = ip.getProcessor();
		this.setSize(400, 300);
		canvas = ip.getWindow().getCanvas();
		xCoordinates = new int[6];
		yCoordinates = new int[6];
		JButton splitButton = new JButton("split");
		JButton percentOfWhite = new JButton("percent of white");
		JPanel panel1 = new JPanel();
		panel1.add(splitButton);
		panel1.add(percentOfWhite);
		this.setContentPane(panel1);
		splitButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				split();
			}
		
		});
		percentOfWhite.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				calculateWhite(IJ.getImage().getProcessor());
			}
			
		});
		canvas.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				handleClick(arg0);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
	private void split (){
		ColorProcessor cp = (ColorProcessor) ip;
		ByteProcessor red = cp.getChannel(1, null);
		ByteProcessor green = cp.getChannel(2, null);
		ByteProcessor blue = cp.getChannel(3, null);
		new ImagePlus("Red", red).show();
		new ImagePlus("Green", green).show();
		new ImagePlus("Blue", blue).show();
	}
	
	private void calculateWhite(ImageProcessor processor){
		int count = 0;
		float percent = 0;
		for (int i=0; i<processor.getWidth(); i++){
			for (int j=0; j<processor.getHeight(); j++){
				if (processor.get(i, j)>=200) {processor.set(i, j, 255); count++;}
				else  processor.set(i, j, 0);
			}		
		}
		int size = processor.getHeight()*processor.getWidth();
		if (count!=0) percent =  100*(float)count/(float)size;
		IJ.showMessage("Procent contains", "White: "+percent);
	}
	
	private void handleClick(MouseEvent event) {
		IJ.showMessage("X: " + event.getX() + "; Y: " + event.getY());
	
	}
	
}
