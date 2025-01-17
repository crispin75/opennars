package nars.guifx.graph2.scene;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.*;
import nars.guifx.JFX;
import nars.guifx.NARfx;
import nars.guifx.annotation.Range;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.VisModel;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.guifx.util.ColorMatrix;
import nars.term.Term;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by me on 10/2/15.
 */
public class DefaultVis<C extends Comparable> implements VisModel<C, DefaultVis.HexTerm2Node<C>> {

    final static Font nodeFont = NARfx.mono(0.25);

    protected double minSize = 16;
    protected double maxSize = 64;

    double nodeScaleCache = 1.0;

    @Range(min = 0, max = 16)
    public final SimpleDoubleProperty nodeScale = new SimpleDoubleProperty(1.0);
    private SpaceGrapher graph = null;
    final Rectangle hoverPanel = new Rectangle();

    public DefaultVis() {
        super();
        nodeScale.addListener((e) -> {
            nodeScaleCache = nodeScale.get();
        });


        hoverPanel.setFill(Color.ORANGE);
        //hoverPanel.setStrokeWidth(6);
        //hoverPanel.setFill(new Color(0.5, 0.25, 0.25, 0.5) /* Color.TRANSPARENT */ );
        //hoverPanel.getStyleClass().add("spaceselector");

        hoverPanel.setSmooth(false);
        hoverPanel.setMouseTransparent(true);
        hoverPanel.setStrokeType(StrokeType.OUTSIDE);

    }
    //public Function<Term,TermNode> nodeBuilder;

    @Override
    public HexTerm2Node<C> newNode(C term) {
        return new HexTerm2Node(term, mouseActivity, mouseUntivity);
    }

    @Override
    public void accept(HexTerm2Node<C> t) {

        if (t == null) {
            return;
        } else {
//            p = t.term.cgetPriority();
//            q = t.c.getQuality();
        }

        //            if (c == null) return;
//
//            double v = 0.5;
//            if (c.hasBeliefs())
//                v = c.getBeliefs().top().getConfidence();
//
//            d.setFill(Color.hsb(v*360,1,1));
//            d.fillRect(0,0,10,10);

        t.scale(minSize + (maxSize - minSize) * t.priNorm);
    }

    final static Font mono = NARfx.mono(8);

    public static final ColorMatrix colors = new ColorMatrix(17 /* op hashcode color, hopefully prime */, 17 /* activation  */,
            (op, activation) -> {
                return Color.hsb(op * 360.0,
                        0.5 + 0.4 * activation,
                        0.3 + activation * 0.65);
            });


    final private AtomicReference<Node> selected = new AtomicReference();

    final EventHandler<MouseEvent> mouseActivity = e -> {
        //if (!hoverPanel.isVisible()) {
        //runLater(() -> {


        if (e.getTarget() instanceof Node) {
            //base.widthProperty().bind( hoverPanel.widthProperty() );
            //base.heightProperty().bind( hoverPanel.heightProperty() );


            Node selected = (Node) e.getTarget();

            if (!(selected instanceof TermNode))
                selected = selected.getParent();
            //TODO recurse n levels up the tree

            setSelected(selected);
        }

        //});
        //}
    };

    final ChangeListener changes = (c, p, v) -> {
        if (this.selected == null) return;
        updateSelection();
    };


    void updateSelection() {
        Node s = selected.get();
        if (s == null) return;

        final Rectangle hp = this.hoverPanel;
        //System.out.print("hoverpanel -> " + v);

        Bounds v = s.localToScene(s.getLayoutBounds());
//        System.out.println("\t" + v);
//        System.out.println("\t\t" + hp.isVisible() + " " + hp.getLayoutBounds());
        //hp.setLayoutX();
        //hp.setLayoutX(v.getMinY());

        //TODO use more efficient transform
        Bounds lb = hp.sceneToLocal(v);
        double ww = lb.getWidth();
        double hh = lb.getHeight();

        double b = 8; //border width

        //hp.setX(0.5*(lb.getMinX()+lb.getMaxX()));
        //hp.setY(0.5*(lb.getMinY()+lb.getMaxY()));
        hp.setX(lb.getMinX() - b);
        hp.setY(lb.getMinY() - b);

        hp.setWidth(ww + b * 2);
        hp.setHeight(hh + b * 2);
    }

    private void setSelected(Node nextSelect) {

        Node prev = selected.getAndSet(nextSelect);
        if (prev == nextSelect) {
            return;
        }


        if (prev != null) {
            prev.translateXProperty().removeListener(changes);
            prev.translateYProperty().removeListener(changes);
        }

        if (nextSelect != null) {
            nextSelect.translateXProperty().addListener(changes);
            nextSelect.translateYProperty().addListener(changes);
            hoverPanel.setVisible(true);
        } else {
            hoverPanel.setVisible(false);
        }


        //hoverPanel.translateXProperty().bind(graph.translate.xProperty());
        //hoverPanel.translateYProperty().bind(graph.translate.yProperty());
        //
        //                hoverPanel.xProperty().bind(n.translateXProperty());
        //                hoverPanel.yProperty().bind(n.translateYProperty());
        //                hoverPanel.setWidth(300.0);
        //                hoverPanel.setHeight(200.0);


    }


    EventHandler<MouseEvent> mouseUntivity = e -> {
        if (hoverPanel.isVisible()) {
            setSelected(null);
            //base.setStroke(null);
            //base.setStrokeWidth(0);
        }
    };


    @Override
    public void start(SpaceGrapher g) {
        if (this.graph != null)
            throw new RuntimeException("already running this vis");

        this.graph = g;
        hoverPanel.setVisible(false);
        hoverPanel.setMouseTransparent(true);


        //TODO see if this is destructive
        g.setOnMouseMoved((MouseEvent e) -> {
            //System.out.println("mouse moved: " + e.getTarget());
            if (this.selected == null) return;
            updateSelection();
        });

        //HACK this is supposed to insert the selection hover directly behind the vertices in NARGraph.. do a more robust index determination here
        g.getChildren().add(0, hoverPanel);
    }


    @Override
    public void stop(SpaceGrapher g) {
        g.getChildren().remove(hoverPanel);
        g.setOnMouseMoved(null); //TODO see if this is destructive
        this.graph = null;
    }

public static class HexTerm2Node<N extends Comparable> extends TermNode<N> {


    private final Canvas base;

    private GraphicsContext g = null;

    private GraphicsContext d = null;

    public HexTerm2Node(N t, EventHandler<MouseEvent> mouseActivity, EventHandler<MouseEvent> mouseUntivity) {
        super(t);


        base = new Canvas();
        base.setLayoutX(-0.5f);
        base.setLayoutY(-0.5f);


        base.setOnMouseClicked(e -> {
            //System.out.println("click " + e.getClickCount());
            if (e.getClickCount() == 2) {
                if (c != null) {
                    //NARfx.run((a, b) -> {
                    System.out.println("doubleclicked " + t);
                    //});
                }
            }
        });

        base.setOnMouseEntered(mouseActivity);

        base.setOnMouseExited(mouseUntivity);

        setPickOnBounds(false);
        setManaged(false);

        //update();

//            base.setLayoutX(-0.5f);
//            base.setLayoutY(-0.5f);

        getChildren().setAll(base);


        render(128, 24); //TODO call this lazily as it is being shown


    }


    /**
     * re-render to image buffer
     */
    public void render(double w, double h) {

        g = base.getGraphicsContext2D();
        d = base.getGraphicsContext2D();


        base.setWidth(w);
        base.setHeight(h);


        //TODO move nodeScaleCache elsewhere
        double s = (1.0 * 4.0) / w; //scaled to width
        base.setScaleX(s);
        base.setScaleY(s);
        base.setScaleZ(s);

        base.setLayoutX(-w / 2);
        base.setLayoutY(-h / 2);


        final double W = base.getWidth();
        final double H = base.getHeight();
        g.clearRect(0, 0, W, H);


        //HACK specific to Term
        if (term instanceof Term) {
            g.setFill(TermNode.getTermColor( ((Term)term), colors, 0.5)); /*colors.get(
                        ,
                        //c==null ? 0 : c.getPriority()) //this can work if re-rendered
                        0.5 //otherwise jus use medium
                ));*/
        }
        g.fillRect(0, 0, W, H);


        g.setFont(mono);
        g.setFill(Color.BLACK);
        g.setFontSmoothingType(FontSmoothingType.LCD);

        g.fillText(term.toString(), 0, H / 2);


    }

}


/**
 * original
 */
public static class HexTermNode extends TermNode {

    private final Text label;
    public final Polygon base;
    private boolean hover = false;

    public HexTermNode(Term t) {
        super(t);

        this.label = new Text(t.toStringCompact());
        base = JFX.newPoly(6, 2.0);


        label.setFill(Color.WHITE);
        label.setBoundsType(TextBoundsType.VISUAL);

        label.setPickOnBounds(false);
        label.setMouseTransparent(true);
        label.setFont(nodeFont);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setSmooth(false);
        //titleBar.setManaged(false);
        //label.setBoundsType(TextBoundsType.VISUAL);

        base.setStrokeType(StrokeType.INSIDE);

        base.setOnMouseClicked(e -> {
            //System.out.println("click " + e.getClickCount());
            if (e.getClickCount() == 2) {
                if (c != null)
                    NARfx.run((a, b) -> {
                        //...
                    });
            }
        });

        EventHandler<MouseEvent> mouseActivity = e -> {
            if (!hover) {
                base.setStroke(Color.ORANGE);
                base.setStrokeWidth(0.05);
                hover = true;
            }
        };
        //base.setOnMouseMoved(mouseActivity);
        base.setOnMouseEntered(mouseActivity);
        base.setOnMouseExited(e -> {
            if (hover) {
                base.setStroke(null);
                base.setStrokeWidth(0);
                hover = false;
            }
        });

        setPickOnBounds(false);


        getChildren().setAll(base, label);//, titleBar);


        //update();

        base.setLayoutX(-0.5f);
        base.setLayoutY(-0.5f);


        label.setLayoutX(-getLayoutBounds().getWidth() / (2) + 0.25);

        base.setCacheHint(CacheHint.SCALE_AND_ROTATE);
        base.setCache(true);

        label.setCacheHint(CacheHint.DEFAULT);
        label.setCache(true);


    }

}


//
//    public double getVertexScaleByPri(Concept c) {
//        return c.getPriority();
//        //return (c != null ? c.getPriority() : 0);
//    }
//
//    public double getVertexScaleByConf(Concept c) {
//        if (c.hasBeliefs()) {
//            double conf = c.getBeliefs().getConfidenceMax(0, 1);
//            if (Double.isFinite(conf)) return conf;
//        }
//        return 0;
//    }


//
//    public Color getEdgeColor(double termMean, double taskMean) {
////            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality
////
////            return Color.hsb(25.0 + 180.0 * (1.0 + (termMean - taskMean)),
////                    0.95f,
////                    Math.min(0.75f + 0.25f * (termMean + taskMean) / 2f, 1f)
////                    //,0.5 * (termMean + taskMean)
////            );
////
//////            return new Color(
//////                    0.5f + 0.5f * termMean,
//////                    0,
//////                    0.5f + 0.5f * taskMean,
//////                    0.5f + 0.5f * (termMean + taskMean)/2f
//////            );
//        return null;
//    }

}
