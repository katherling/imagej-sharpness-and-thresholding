
import java.awt.*;
import java.awt.event.*;

import ij.*;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.UnsharpMask;
import ij.process.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	
	
	private ImageProcessor ip;
	private ImageProcessor original;
	private ImagePlus imp;
	private ImagePlus r;
	private ImagePlus g;
	private ImagePlus b;
	private ImageProcessor rOriginal;
	private ImageProcessor gOriginal;
	private ImageProcessor bOriginal;
	private final static int WIDTH = 400;
	private final static int HEIGHT = 200;
	public MainFrame(ImageProcessor ip, ImagePlus imp) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (Exception e) {	}
		
		this.ip = ip;
		original = ip.duplicate();
		this.imp = imp;
		setSize(WIDTH, HEIGHT);
		setLayout(new BorderLayout());
		Toolkit kit = Toolkit.getDefaultToolkit();
		setLocation(new Point( (kit.getScreenSize().width - WIDTH) / 2,
							   (kit.getScreenSize().height - HEIGHT) / 2));
			
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Регулировка резкости", getSharpnessPanel());
		pane.addTab("Пороговая фильтрация", getThresholdPanel());
		
		getContentPane().add(pane, BorderLayout.CENTER);
	}
	
	public JComponent getSharpnessPanel() {
		Box sharpnessTab = Box.createVerticalBox();
		
		final JSlider slider = new JSlider(1, 200, 1);
		final JLabel sliderLabel = new JLabel("0.1");
		Box sliderBox = Box.createHorizontalBox();

		sliderLabel.setMinimumSize(new Dimension(25, 10));
		sliderLabel.setPreferredSize(new Dimension(25, 10));
		sliderBox.add(sliderLabel);
		sliderBox.add(slider);
		
		Box optionsBox = Box.createVerticalBox();
		
		final JCheckBox autoUpdateCheckBox = new JCheckBox("Автоматическое обновление");
		final JButton updateButton = new JButton("Обновить");
		
		optionsBox.add(autoUpdateCheckBox);
		
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent ev) {
				float sigma = (float)(slider.getValue()) / 10;
				sliderLabel.setText(Float.toString(sigma));
				if (autoUpdateCheckBox.isSelected() || ev.getSource() == updateButton)
					UnsharpFilter(sigma);
			}
		});
		
		updateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				slider.getChangeListeners()[0].stateChanged(new ChangeEvent(updateButton));
			}
			
		});
		
		Box sharpnessBox = Box.createVerticalBox();
		final JLabel sharpnessLabel = new JLabel("Резкость: не пересчитана");
		JButton calculateSharpnessButton = new JButton("Оценить");
		sharpnessBox.add(sharpnessLabel);
		
		calculateSharpnessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double sharpness = CalculateRelativeSharpness();
				sharpnessLabel.setText("Резкость: " + Double.toString(sharpness));
			}
			
		});
		
		Box buttonsBox = Box.createHorizontalBox();
		buttonsBox.add(Box.createHorizontalGlue());
		buttonsBox.add(calculateSharpnessButton);
		buttonsBox.add(Box.createHorizontalStrut(10));
		buttonsBox.add(updateButton);
		
		sharpnessTab.add(sliderBox);
		sharpnessTab.add(optionsBox);
		sharpnessTab.add(sharpnessBox);
		sharpnessTab.add(Box.createVerticalGlue());
		sharpnessTab.add(buttonsBox);
		sharpnessTab.add(Box.createVerticalStrut(10));
		JPanel res = new JPanel(new BorderLayout());
		res.add(sharpnessTab, BorderLayout.CENTER);
		return res;
	}
	
	public JComponent getThresholdPanel() {
		JPanel res = new JPanel(new BorderLayout());
		Box mainBox = Box.createVerticalBox();
		
		JButton splitButton = new JButton("Разделить на компоненты");
		splitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SplitImage();
			}
		});
		
		Box filteringBox = Box.createHorizontalBox();
		
		ButtonGroup colorPicker = new ButtonGroup();
		Box radiosBox = Box.createVerticalBox();
		final JRadioButton redRadio = new JRadioButton("Красный");
		final JRadioButton greenRadio = new JRadioButton("Зеленый");
		final JRadioButton blueRadio = new JRadioButton("Синий");
		colorPicker.add(redRadio);
		colorPicker.add(greenRadio);
		colorPicker.add(blueRadio);
		
		radiosBox.setBorder(BorderFactory.createTitledBorder("Канал"));
		redRadio.doClick();
		radiosBox.add(redRadio);
		radiosBox.add(greenRadio);
		radiosBox.add(blueRadio);
		
		final JCheckBox autoCheckBox = new JCheckBox("Автоматическое обновление");
		autoCheckBox.doClick();
		
		final JCheckBox blackAndWhiteCheckBox = new JCheckBox("Черно-белый");
		
		final JSlider upperThresholdSlider = new JSlider(0, 255, 0);
		final JSlider lowerThresholdSlider = new JSlider(0, 255, 0);
		final JButton filterButton = new JButton("Отфильтровать");
		filterButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent ev) {
				ImagePlus channel = null;
				ImageProcessor original = null;
				if (redRadio.isSelected()) {
					channel = r;
					original = rOriginal;
				}
				if (greenRadio.isSelected()) {
					channel = g;
					original = gOriginal;
				}
				if (blueRadio.isSelected()) {
					channel = b;
					original = bOriginal;
				}
				
				if (channel != null && original != null) {
					Integer filteredValue = null;
					if (blackAndWhiteCheckBox.isSelected()) {
						filteredValue = new Integer(0);
					}
					thresholdFilter(channel.getProcessor(), original, 
									upperThresholdSlider.getValue(), lowerThresholdSlider.getValue(), 255, filteredValue);
					channel.updateAndDraw();
				}
			}
		});
		
		blackAndWhiteCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				filterButton.doClick();
			}
			
		});
		
		Box thresholdBox = Box.createVerticalBox();
		Box upperThresholdBox = Box.createHorizontalBox();
		final JLabel upperThresholdLabel = new JLabel("0");
		upperThresholdSlider.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent arg0) {
				upperThresholdLabel.setText(Integer.toString(upperThresholdSlider.getValue()));
				if (lowerThresholdSlider.getValue() > upperThresholdSlider.getValue()) {
					lowerThresholdSlider.setValue(upperThresholdSlider.getValue());
				}
				if (autoCheckBox.isSelected()) {
					filterButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, ""));
				}
			}
		});
		
		Box lowerThresholdBox = Box.createHorizontalBox();
		final JLabel lowerThresholdLabel = new JLabel("0");
		lowerThresholdSlider.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent arg0) {
				lowerThresholdLabel.setText(Integer.toString(lowerThresholdSlider.getValue()));
				if (upperThresholdSlider.getValue() < lowerThresholdSlider.getValue()) {
					upperThresholdSlider.setValue(lowerThresholdSlider.getValue());
				}
				if (autoCheckBox.isSelected()) {
					filterButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, ""));
				}
			}
		});

		upperThresholdLabel.setMinimumSize(new Dimension(25, 10));
		upperThresholdLabel.setPreferredSize(new Dimension(25,10));
		upperThresholdBox.add(upperThresholdLabel);
		upperThresholdBox.add(upperThresholdSlider);

		lowerThresholdLabel.setMinimumSize(new Dimension(25, 10));
		lowerThresholdLabel.setPreferredSize(new Dimension(25,10));
		lowerThresholdBox.add(lowerThresholdLabel);
		lowerThresholdBox.add(lowerThresholdSlider);
		
		thresholdBox.add(upperThresholdBox);
		thresholdBox.add(lowerThresholdBox);
		
		Box rightBox = Box.createVerticalBox();
		rightBox.add(thresholdBox);
		rightBox.add(autoCheckBox);
		rightBox.add(blackAndWhiteCheckBox);
		
		filteringBox.add(radiosBox);	
		filteringBox.add(rightBox);
		
		
		
		mainBox.add(filteringBox);
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(filterButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(splitButton);
		
		mainBox.add(buttonBox);
		
		
		res.add(mainBox, BorderLayout.CENTER);
		return res;
	}
	
	public void SplitImage() {
		if (ip instanceof ColorProcessor == false) {
			return;
		}
		
		ColorProcessor cp = (ColorProcessor) ip;
		r = new ImagePlus("Красный канал", cp.getChannel(1, null));
		g = new ImagePlus("Зеленый канал", cp.getChannel(2, null));
		b = new ImagePlus("Синий канал", cp.getChannel(3, null));
		
		r.show(); g.show(); b.show();
		rOriginal = r.getProcessor().duplicate();
		gOriginal = g.getProcessor().duplicate();
		bOriginal = b.getProcessor().duplicate();
	}
	
	public void thresholdFilter(ImageProcessor channel, ImageProcessor original, int upper, int lower, int replaceValue, Integer filteredValue) {

		if (channel == null)
			return;
		
		int w = channel.getWidth();
		int h = channel.getHeight();
		for (int x=0; x<w; x++)
			for (int y=0; y<h; y++) {
				int value = original.get(x, y);
				int filtered = value;
				if (filteredValue != null) {
					filtered = filteredValue.intValue();
				}
				channel.set(x, y, inRange(value, lower, upper) ? replaceValue : filtered);
			}
	}
	
	boolean inRange(int value, int lower, int upper) {
		return value >= lower && value <= upper;
	}
	
	public void InvertImage() {
		int w = ip.getWidth();
		int h = ip.getHeight();
		for (int x=0; x<w; x++)
			for (int y=0; y<h; y++) {
				int p = ip.get(x, y);
				ip.set(x, y, 255-p);
			}
		
		imp.updateAndDraw();
	}
	
	public void UnsharpFilter(float sigma) {
		float[] kernel = makeGaussKernel1d(sigma);
		FloatProcessor R = original.toFloat(0, null);
		LaplacianFilter(R, sigma, kernel);
		FloatProcessor G = original.toFloat(1, null);
		LaplacianFilter(G, sigma, kernel);
		FloatProcessor B = original.toFloat(2, null);
		LaplacianFilter(B,sigma, kernel);
		
		ColorProcessor cp = (ColorProcessor) ip;
		
		cp.setPixels(0, R);
		cp.setPixels(1, G);
		cp.setPixels(2, B);
		
		imp.updateAndDraw();
	}
	
	public void LaplacianFilter(ImageProcessor imageProc, float sigma, float[] kernel) {
		ImageProcessor I = imageProc.convertToFloat();
		
		ImageProcessor J = I.duplicate();
		float[] H = makeGaussKernel1d(sigma);
		Convolver cv = new Convolver();
		cv.setNormalize(true);
		cv.convolve(J, H, 1, H.length);
		cv.convolve(J, H, H.length, 1);
		
		I.multiply(2);
		I.copyBits(J,0,0,Blitter.SUBTRACT);
		
		imageProc.insert(I.convertToByte(false), 0, 0);
	}
	
	float[] makeGaussKernel1d(double sigma) {

		// create the kernel
		int center = (int) (3.0*sigma);
		float[] kernel = new float[2*center+1]; 
		
		double sigma2 = sigma * sigma;
		for (int i=0; i<kernel.length; i++) {
			double r = center - i;
			kernel[i] = (float) Math.exp(-0.5 * (r*r) / sigma2);
		}
		
		return kernel;
	}
	
	double CalculateRelativeSharpness() {
		
		ImageProcessor p = ip.duplicate();
		p.findEdges();		
		return p.getStatistics().mean;
	}
	
}
