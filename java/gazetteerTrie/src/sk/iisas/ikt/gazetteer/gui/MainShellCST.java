package sk.iisas.ikt.gazetteer.gui;

// import java.io.IOException;
// import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
// import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sk.iisas.ikt.gazetteer.common.PositionLengthHolder;
import sk.iisas.ikt.gazetteer.cst.TextAnalyzer;
import sk.iisas.ikt.gazetteer.cst.ListParser;
import sk.iisas.ikt.gazetteer.cst.TreeNode;

/**
 * hlavna GUI trieda
 * Autor: Adam Pomothy
 * Modification for IKT: Giang Nguyen
 */

public class MainShellCST {
	// private static final Logger logger = Logger.getLogger("sk.iisas.ikt.tokenizer.impl.ListParser");

	private Shell shell;

	private String textFilePathStr;
	private Text textFilePathText;

	private String listFilePathStr;
	private Text listFilePathText;

	private ListParser listParser;
	private TextAnalyzer textAnalyzer;

	StyledText textOutcome;
	StyledText textCounts;

	public MainShellCST(Display display) {
		listParser = new ListParser();
		textAnalyzer = new TextAnalyzer();

		shell = new Shell(display);
		shell.setText("Tokenizer");
		initUI();

		// nasetujeme velkost a poziciu
		shell.setSize(1024, 600);
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void initUI() {
		GridLayout gl = new GridLayout(6, false);
		gl.horizontalSpacing = 8;
		gl.verticalSpacing = 8;
		gl.marginBottom = 5;
		gl.marginTop = 5;
		shell.setLayout(gl);

		// -----------------------------------------------------------------TEXT
		// CHOOSER
		Label textFileChooserLabel = new Label(shell, SWT.LEFT);
		textFileChooserLabel.setText("Text file:");

		// text filePathText
		textFilePathText = new Text(shell, SWT.SINGLE | SWT.LEFT
				| SWT.READ_ONLY | SWT.BORDER);
		textFilePathText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				MainShellCST.this.textFilePathStr = ((Text) e.widget).getText();
			}
		});

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 20;
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		textFilePathText.setLayoutData(gridData);

		Button chooseTestFileButton = new Button(shell, SWT.PUSH);
		chooseTestFileButton.setText("Open");
		chooseTestFileButton.setBounds(20, 50, 80, 30);
		chooseTestFileButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(shell);
				String path = dialog.open();
				if (path != null) {
					MainShellCST.this.textFilePathText.setText(path);
				}
			}
		});

		// -----------------------------------------------------------------LIST
		// CHOOSER

		Label listFileChooserLabel = new Label(shell, SWT.LEFT);
		listFileChooserLabel.setText("List file:");

		// text filePathText
		listFilePathText = new Text(shell, SWT.SINGLE | SWT.LEFT
				| SWT.READ_ONLY | SWT.BORDER);
		listFilePathText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				MainShellCST.this.listFilePathStr = ((Text) e.widget).getText();
			}
		});

		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 20;
		// gridData.widthHint = 90;
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		listFilePathText.setLayoutData(gridData);

		Button chooseListFileButton = new Button(shell, SWT.PUSH);
		chooseListFileButton.setText("Open");
		chooseListFileButton.setBounds(20, 50, 80, 30);
		chooseListFileButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(shell);
				String path = dialog.open();
				if (path != null) {
					MainShellCST.this.listFilePathText.setText(path);
				}
			}
		});

		// -----------------------------------------------------------------TEXTs
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;

		textOutcome = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
		textOutcome.setLayoutData(gridData);

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;

		textCounts = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
		textCounts.setLayoutData(gridData);

		// -----------------------------------------------------------------SEARCH
		// BUTTON
		Button searchButton = new Button(shell, SWT.PUSH);
		searchButton.setText("                  Search                  ");
		searchButton.setSize(80, 30);
		searchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				listParser = new ListParser();
				textAnalyzer = new TextAnalyzer();

				if (!MainShellCST.this.listFilePathStr.equals("") && !MainShellCST.this.textFilePathStr.equals("")) {
					
					TreeNode rootNode = MainShellCST.this.listParser.parseListToTree(MainShellCST.this.listFilePathStr);

					MainShellCST.this.textAnalyzer.setRootNode(rootNode);
					MainShellCST.this.textAnalyzer.searchText(MainShellCST.this.textFilePathStr);

					Iterator<Entry<TreeNode, Integer>> it = MainShellCST.this.textAnalyzer.getCountHashMap().entrySet().iterator();
					StringBuilder strBuilder = new StringBuilder();
					while (it.hasNext()) {
						Map.Entry<TreeNode, Integer> pairs = (Map.Entry<TreeNode, Integer>) it.next();
						strBuilder.append(pairs.getKey().getEntityName());
						strBuilder.append(": ");
						strBuilder.append(pairs.getValue());
						strBuilder.append("\n");
						it.remove();
					}
					textCounts.setText(strBuilder.toString());

                    Double fileSize = new Double(TextAnalyzer.getSizeOfFile(MainShellCST.this.textFilePathStr));
                    if (fileSize.compareTo(new Double(20)) > 0) {
                        textOutcome.setText("Document too long for SWT styled text");
                    } else {
                        textOutcome.setText(TextAnalyzer
                                .readTextFileToString(MainShellCST.this.textFilePathStr));
                        colorOutcomeText(shell, textOutcome,
                                textAnalyzer.getPositionsLengthList());
                    }

				}
			}
		});

		gridData = new GridData();
		gridData.heightHint = 40;
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.CENTER;
		searchButton.setLayoutData(gridData);
	}

	private void colorOutcomeText(Shell shell, StyledText text,
			List<PositionLengthHolder> positionsLengths) {
		Color orange = new Color(shell.getDisplay(), 255, 127, 0);

		for (PositionLengthHolder plh : positionsLengths) {
			StyleRange styleRange = new StyleRange();
			styleRange.start = plh.position;
			styleRange.length = plh.length;
			styleRange.fontStyle = SWT.BOLD;
			styleRange.foreground = orange;
			text.setStyleRange(styleRange);
		}
	}

	public static void main(String[] args) {
		Display display = new Display();
		new MainShellCST(display);
		display.dispose();
	}

}
