import java.awt.Rectangle;
import java.awt.MouseInfo;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JPanel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A {@code JPanel} whose graphics is draggable, zoomable, and rotatable.
 *
 * <p>
 * Listeners are implemented to listen to specific user's actions. In addition,
 * APIs are provided to simulate such actions. See the {@code drag},
 * {@code rotate}, {@code zoom} methods.
 *
 * <p>
 * To drag the graphics on this panel, press, hold, and drag the mouse. To zoom,
 * scroll the mouse wheel. To rotate, press and hold the Ctrl key while
 * pressing, holding, and dragging the mouse.
 *
 * <p>
 * When extending from this class, it is important to invoke this class's
 * constructor in the child class's constructor (i.e. {@code super()}), and
 * invoke this class's {@code paintComponent} in the child class's
 * {@code paintComponent} method.
 *
 * @author Zach
 * @version 0.1.0
 * @see #drag(int, int)
 * @see #rotate(double)
 * @see #zoom(double, double, double)
 */
public abstract class TransformPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    /**
     * The current (net) amount of translation on the x-axis in double precision.
     */
    private double translateX = 0;
    /**
     * The current (net) amount of translation on the y-axis in double precision.
     */
    private double translateY = 0;
    /**
     * The current (net) amount of scale.
     */
    private double scale = 1.0;
    /**
     * The current (net) amount of rotation, in radians.
     */
    private double rotate = 0.0;
    /**
     * The bounding box of the focused object of this panel; the center of scale
     * and rotation is the center of the box.
     */
    private Rectangle focus;

    // For mouse drags
    private int prevMouseX;
    private int prevMouseY;
    private boolean isRotating = false;

    /**
     * Constructs a new {@code TransformPanel} with no focus and all the listeners
     * added.
     */
    public TransformPanel() {
        super();

        this.focus = new Rectangle();

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addKeyListener(this);

        this.setFocusable(true);
    }

    /**
     * Returns the bounding box of the focused object in this panel.
     *
     * @return the bounding box of the focused object in this panel.
     */
    public Rectangle getFocus() {
        return this.focus;
    }

    /**
     * Sets the bounding box of the focused object in this panel.
     *
     * @param focus the bounding box of the focused object in this panel
     */
    public void setFocus(Rectangle focus) {
        this.focus = focus;
    }

    /**
     * Simulates a drag action.
     *
     * <p>
     * Input values' signs indicate direction according to Java Swing's rules.
     *
     * @param x the distance on the x-axis that is dragged
     * @param y the distance on the y-axis that is dragged
     */
    public void drag(int x, int y) {
        this.translateX += x;
        this.translateY += y;

        this.repaint();
    }

    /**
     * Simulates a rotate action.
     *
     * @param angle the angle of rotation, in radians
     */
    public void rotate(double angle) {
        this.rotate += angle;

        this.repaint();
    }

    /**
     * Simulates a zoom action.
     *
     * @param factor  the factor of scale e.g. {@code factor} = 2 means double the
     *                current amount of zoom
     * @param targetX the x coordinate of the center of scale
     * @param targetY the y coordinate of the center of scale
     */
    public void zoom(double factor, double targetX, double targetY) {
        if (this.scale * factor < 0) {
            return;
        }

        double prevScaleX = this.scale;
        double prevScaleY = this.scale;

        this.scale *= factor;

        double scaleQuotientX = this.scale / prevScaleX;
        double scaleQuotientY = this.scale / prevScaleY;

        this.translateX = scaleQuotientX * this.translateX + (1 - scaleQuotientX) * targetX;
        this.translateY = scaleQuotientY * this.translateY + (1 - scaleQuotientY) * targetY;
    }

    /**
     * Centers the focused object on the panel while keeping the current rotation
     * and zoom level.
     *
     * <p>
     * Technically, centering is done such that the center of the bounding box
     * will be on the center of this panel.
     */
    public void center() {
        double currentScale = this.scale;

        this.zoom(1.0 / currentScale, 0, 0);

        this.translateX = -this.focus.getX() + (this.getWidth() - this.focus.getWidth()) / 2.0;
        this.translateY = -this.focus.getY() + (this.getHeight() - this.focus.getHeight()) / 2.0;

        this.zoom(currentScale, this.getWidth() / 2.0, this.getHeight() / 2.0);

        this.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.prevMouseX = e.getX();
        this.prevMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (!isRotating) {
            this.drag(x - prevMouseX, y - prevMouseY);
        } else {
            this.rotate((y - prevMouseY) * Math.PI / 500.0);
        }

        this.prevMouseX = x;
        this.prevMouseY = y;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int unitsToScroll = e.getUnitsToScroll();

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        Point panelLocation = this.getLocationOnScreen();

        double mouseX = mouseLocation.getX() - panelLocation.getX();
        double mouseY = mouseLocation.getY() - panelLocation.getY();

        this.zoom((this.scale + unitsToScroll / -10.0) / this.scale, mouseX, mouseY);

        this.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            this.isRotating = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            this.isRotating = false;
        }
    }

    /**
     * Draw graphics on this panel, with transforms from drag, zoom, and rotate
     * actions applied.
     *
     * <p>
     * For children classes: invoke {@code super.paintComponent(g)} for the
     * transforms to take effect.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(this.translateX, this.translateY);
        g2d.scale(this.scale, this.scale);
        g2d.rotate(this.rotate, this.focus.getX() + this.focus.getWidth() / 2.0, this.focus.getY() + this.focus.getHeight() / 2.0);
    }
}