package main.java;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class BFGui extends JFrame implements ActionListener, MouseListener, KeyListener {
	/**
	 * Bifurcations in a logistic map
	 * TCU Physics
	 * 1/17/2017
	 * @author Cole H. Turner
	 */
	private static final long serialVersionUID = 1L;
	private static final int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
	private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	private static Font regFont = new Font(Font.DIALOG_INPUT, Font.BOLD, resolution / 4);
	private static Font subFont = new Font(Font.DIALOG_INPUT, Font.BOLD, resolution / 6);


	private double minx, miny, rangex, rangey, res, initPop, scalex, scaley;
	private int delay, insetx, insety, width, height;
	private double[] pop;
	private boolean reset, stop, step, drawing;
	private Rectangle box;

	private Timer timer;

	public BFGui(){
		super("Bifurcations");
		init();
		Container pane = new JPanel(){
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics gr){
				Graphics2D g = (Graphics2D) gr;
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.white);
				g.setFont(regFont);
				g.drawString("Bifurcation Diagram for the Logistic Map", insetx, insety / 3);
				g.drawString("x  = \u03BBx (1-x )", width / 2, insety * 3 / 4);
				g.drawString("x", insetx - 20, insety - 20);
				g.drawString("\u03BB", insetx + width + 20, insety + height + 20);
				g.drawString("SPACE > Play/Pause", insetx, insety * 5 / 4 + height);
				g.drawString("ENTER > Reset (" + (initPop < 0 ? "random" : "\u21C5 " + String.format("%.3f", initPop)) + ")", insetx, insety * 3 / 2 + height);
				g.drawString(" \u27A1   > Step Forward", insetx + 15, insety * 7 / 4 + height);

				g.setFont(subFont);
				g.drawString("                  n", width / 2, insety * 4 / 5);
				g.drawString("  n+1      n", width / 2 - 10, insety * 4 / 5);



				g.translate(insetx, screenSize.height - insety);
				double mouse[] = getMouseCoords();
				int mx, my;
				if(isInBounds(mouse)){
					mx = (int)((mouse[0] - minx) * scalex);
					my = -(int)((mouse[1] - miny) * scaley);
					g.drawLine(mx, 0, mx, my);
					g.drawLine(0, my, mx, my);
					g.drawString(String.format("%.3f", mouse[0]), mx + 10, -10);
					g.drawString(String.format("%.3f", mouse[1]), 10, my - 10);
				}

				int l = 20;
				if(initPop >= miny && initPop <= miny + rangey){
					g.drawLine(-l, -(int)(initPop * scaley), width, -(int)(initPop * scaley));
					g.setFont(regFont);
					g.drawString("x", -l - 60, -(int)(initPop * scaley) + 15);
					g.setFont(subFont);
					g.drawString("0", -l - 25, -(int)(initPop * scaley) + 35);
				}

				g.scale(1, -1);
				g.drawLine(0, 0, 0, height);
				g.drawLine(0, 0, width, 0);
				for(int d = (int)(minx) + 1; d <= minx + rangex; d++){
					if(d != 0.0) g.drawLine((int)((d - minx) * scalex), l, (int)((d - minx) * scalex), -l);
				}
				if(miny == 0.0) g.drawLine(-l, height, l, height);
				if(drawing) {
					if(isInBounds(mouse))
						box.setSize((int)(mouse[0] * scalex) - box.x, height);
					g.draw(box);
				}
				g.setColor(Color.GREEN);
				if(reset) reset();
				else {
					double x;
					for(int i = 0; i < pop.length; i++){
						x = minx + (i / (res * scalex));
						if(!stop || step){
							pop[i] = x * pop[i] * (1 - pop[i]);
						}
						if(pop[i] >= miny && pop[i] <= miny + rangey)
							g.drawRect((int)(i / res), (int)((pop[i] - miny) * scaley), 1, 1);
					}
					if(step) step = false;
				}
			}
		};

		this.setSize(screenSize);
		this.setContentPane(pane);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.setUndecorated(true);
		this.setVisible(true);

		timer = new Timer(delay, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				repaint();
			}
		});
		timer.start();
	}

	public void init(){
		minx = 0.0;
		miny = 0.0;
		rangex = 4.0;
		rangey = 1.0;
		delay = 50;
		res = 10.0;
		initPop = -1.0;
		stop = true;
		step = false;
		reset = true;
		insetx = 100;
		insety = 300;
		width = (screenSize.width - 2 * insetx);
		height = (screenSize.height - 2 * insety);
		scalex = width / rangex;
		scaley = height / rangey;
		pop = new double[(int)(width * res)];
	}

	public void reset(){
		if(initPop < 0){
			for(int i = 0; i < pop.length; i++)
				pop[i] = miny + Math.random() * rangey;
		} else for(int i = 0; i < pop.length; i++)
			pop[i] = initPop;
		reset = false;
	}

	public void rescale(Rectangle r){
		minx = r.x / scalex;
		miny = r.y / scaley;
		rangex = r.width / scalex;
		rangey = r.height / scaley;
		scalex = width / rangex;
		scaley = height / rangey;
		reset();
	}

	public double[] getMouseCoords(){
		Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
		return new double[]{(mouseLoc.getX() - insetx) / scalex + minx, miny + rangey - (mouseLoc.getY() - insety) / scaley};
	}

	public boolean isInBounds(double[] arr){
		return arr[0] <= minx + rangex && arr[1] <= miny + rangey && arr[0] >= minx && arr[1] >= miny;
	}

	public void actionPerformed(ActionEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		double[] mouse = getMouseCoords();
		switch(e.getButton()){
		case MouseEvent.BUTTON3 :
			if(isInBounds(mouse)){
				initPop = mouse[1];
			} else initPop = -1.0;
			stop = true;
			reset = true;
			break;
		case MouseEvent.BUTTON1 :
			box = new Rectangle((int)(mouse[0] * scalex), 0, 0, 0);
			drawing = true;
			break;
		}
	}
	public void mouseReleased(MouseEvent e) {
		switch(e.getButton()){
		case MouseEvent.BUTTON1 :
			drawing = false;
			if(box.width * box.height != 0){
				rescale(box);
				box.width = 0;
			}
			break;
		}
	}
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()){
		case KeyEvent.VK_RIGHT :
			if(stop) step = true;
			break;
		case KeyEvent.VK_UP :
			stop = true;
			if(initPop < 1.0){
				initPop += 0.001;
				reset = true;
			} else initPop = 1.0;
			break;
		case KeyEvent.VK_DOWN :
			stop = true;
			if(initPop == 0.0 || initPop > 0.0015){
				initPop -= 0.001;
			} else if(initPop > 0.0){
				initPop = 0.0;
			}
			else break;
			reset = true;
			break;
		case KeyEvent.VK_SPACE :
			stop = !stop;
			break;
		case KeyEvent.VK_ENTER :
			reset = true;
			break;
		case KeyEvent.VK_F1 :
			init();
			break;
		case KeyEvent.VK_ESCAPE :
			System.exit(0);
		}
	}
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()){
		}
	}
	public void keyTyped(KeyEvent e) {}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new BFGui();
			}
		});
	}
}
