package application;

import java.util.ArrayList;
import java.util.Stack;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import sequenceAlignment.Cell;
import sequenceAlignment.longestCommonSubsequence.LCS;
import sequenceAlignment.needlemanWunsch.SequenceAlignmentWunsch;
import sequenceAlignment.smithWaterman.Smith_waterman;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class Main extends Application {

	//Enum for our algorithm types
	public enum algorithmType {
		LCS, // Longest common subsequence
		NMW, // Needleman Wunsch
		SWM // Smith Waterman
	};
	//The currently selected algorithm
	private algorithmType selectedAlgorithm = algorithmType.LCS;
	
	Stage window;
	Scene mainScene, secondScene;

	BorderPane mainLayout = new BorderPane();
	ScrollPane scroll = new ScrollPane();
	HBox hbox = new HBox();
	HBox hboxSequence1 = new HBox();
	HBox hboxSequence2 = new HBox();
	VBox vboxSequences = new VBox();
	HBox hboxBottomBox = new HBox();
	GridPane grid = new GridPane();
	StackPane stack = new StackPane();

	//These are our interactable GUI objects
	TextField txtInput = new TextField();
	TextField txtInput2 = new TextField();
	Button btnClear = new Button("Clear");
	Button btnTest = new Button("Run Algorithm");
	Button btnNext = new Button("Get Traceback");
	CheckBox checkLCS = new CheckBox("LCS");
	CheckBox checkNMW = new CheckBox("Needleman Wunsch");
	CheckBox checkSWM = new CheckBox("Smith Waterman");
	Label bottomSequenceLabel1 = new Label("Sequence 1:");
	Label bottomSequenceLabel2 = new Label("Sequence 2:");
	// create the Grid class, this is to get it out of main
	Grid mainGrid = new Grid(grid);
	
	ArrayList<Cell> tracebackCellList;
	int k=0;

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			window = primaryStage;
			primaryStage.setTitle("Sequence Aligner");

			// Grid starts out invisible
			grid.setVisible(false);
			
			// Only one check box can be active at a time. Start program with LCS active.
			checkLCS.setSelected(true);
			checkNMW.setSelected(false);
			checkSWM.setSelected(false);

			// First we set up the vbox & hbox's which contain the top layout with the text fields and buttons
			
			//This first HBox contains the first sequence text input box
			Label sequenceLabel = new Label("Sequence 1:");
			hboxSequence1.setSpacing(10);
			hboxSequence1.setAlignment(Pos.CENTER_LEFT);
			hboxSequence1.getChildren().addAll(sequenceLabel, txtInput);
			
			//This second HBox contains the second sequence text input box
			sequenceLabel = new Label("Sequence 2:");
			hboxSequence2.setSpacing(10);
			hboxSequence2.setAlignment(Pos.CENTER_LEFT);
			hboxSequence2.getChildren().addAll(sequenceLabel, txtInput2);
			
			//This VBox contains positions the text inputs on top of one another.
			vboxSequences.setSpacing(10);
			vboxSequences.setMaxSize(300, 300);
			vboxSequences.setPadding(new Insets(15, 10, 10, 15));
			vboxSequences.getChildren().addAll(hboxSequence1, hboxSequence2);
			
			//Finally this last HBox organized the text inputs with the buttons and check boxes.
			hbox.getChildren().add(vboxSequences);
			hbox.setAlignment(Pos.CENTER);
			hbox.getChildren().addAll(btnTest, btnClear, checkLCS, checkNMW, checkSWM);
			hbox.setBorder(Border.EMPTY);
			hbox.setPadding(new Insets(5, 5, 5, 5));
			hbox.setSpacing(10);

			//This HBox is for the bottom box
			hboxBottomBox.setAlignment(Pos.CENTER_LEFT);
			hboxBottomBox.setSpacing(30);
			hboxBottomBox.setPadding(new Insets(15, 10, 10, 15));
			hboxBottomBox.getChildren().addAll(btnNext, bottomSequenceLabel1, bottomSequenceLabel2);
			
			//TEXT INPUT TEXT FORMATTER ALLOWS ONLY A C G T and U to be entered
			//Text inputs cannot share the same formatter so we need two.
			TextFormatter<String> textFormatterSeq1 = new TextFormatter<>( change -> {
			    if (!change.isContentChange()) {
			        return change;
			    }

			    Character text;
			    //Grab the character that was entered into the text input, if size = 0 then we are at the first entry.
			    if (change.getControlNewText().length() > 0)
			    	text = change.getControlNewText().charAt(change.getControlNewText().length() - 1);
			    else
			    	return change;
			    //If it is the correct letter, allow the change. Otherwise throw it away.
			    if (text.equals('a') || text.equals('c') || text.equals('g') || text.equals('t') ||  text.equals('u')) {
			        return change;
			    }
			    return null;
			});
			
			TextFormatter<String> textFormatterSeq2 = new TextFormatter<>( change -> {
			    if (!change.isContentChange()) {
			        return change;
			    }

			    Character text;
			    //Grab the character that was entered into the text input, if size = 0 then we are at the first entry.
			    if (change.getControlNewText().length() > 0)
			    	text = change.getControlNewText().charAt(change.getControlNewText().length() - 1);
			    else
			    	return change;
			    //If it is the correct letter, allow the change. Otherwise throw it away.
			    if (text.equals('a') || text.equals('c') || text.equals('g') || text.equals('t') ||  text.equals('u')) {
			        return change;
			    }
			    return null;
			});
			txtInput.setTextFormatter(textFormatterSeq1);
			txtInput2.setTextFormatter(textFormatterSeq2);
			
			
			// Here we set up of side panel for information, to be expanded on later, uses a stackpane
			// Label info = new Label("Info goes Here");
			// StackPane.setAlignment(info, Pos.CENTER);
			// stack.getChildren().add(info);
			stack.setPadding(new Insets(15, 12, 10, 15));
			stack.setBorder(Border.EMPTY);
			stack.setMinWidth(300);
			stack.setStyle("-fx-border-style:solid; -fx-border-color: black;");

			// add the layouts to the main layout
			mainLayout.setTop(hbox);
			mainLayout.setRight(stack);
			mainLayout.setCenter(scroll);
			mainLayout.setBottom(hboxBottomBox);
			mainScene = new Scene(mainLayout, 900, 400);

			// create the window and initialize
			window.setScene(mainScene);
			window.show();
			
			
			//CHECK BOX EVENT HANDLERS GO BELOW HERE
			

			checkLCS.setOnAction(e -> {
				// Only one check box can be active at a time.
				checkLCS.setSelected(true);
				checkNMW.setSelected(false);
				checkSWM.setSelected(false);

				selectedAlgorithm = algorithmType.LCS;
			});

			checkNMW.setOnAction(e -> {
				// Only one check box can be active at a time.
				checkLCS.setSelected(false);
				checkNMW.setSelected(true);
				checkSWM.setSelected(false);

				selectedAlgorithm = algorithmType.NMW;
			});

			checkSWM.setOnAction(e -> {
				// Only one check box can be active at a time.
				checkLCS.setSelected(false);
				checkNMW.setSelected(false);
				checkSWM.setSelected(true);

				selectedAlgorithm = algorithmType.SWM;
			});
			
			
			// BUTTON EVENT HANDLERS GO BELOW HERE


			// Clear button resets the grid view.
			btnClear.setOnAction(e -> {

				// Remove the grid elements, the stack elements and set the grid visibility to
				// false so the test button doesn't work.
				grid.getChildren().clear();
				grid.setStyle("-fx-border-style:none ");
				stack.getChildren().clear();
				mainLayout.getChildren().remove(grid);
				grid.setVisible(false);
				
				bottomSequenceLabel1.setText("Sequence 1:");
				bottomSequenceLabel2.setText("Sequence 2:");

			});

			// Test button runs the selected algorithm.
			btnTest.setOnAction(e -> {

				// If the create button hasn't been pressed, don't run the algorithm
				if (txtInput.getText().isEmpty() || txtInput2.getText().isEmpty())
					return;
				
				//If the sequences have been changed we need to recreate the grid
				String[] inputedSequences = mainGrid.getSequences();
				if (txtInput.getText() != inputedSequences[0]|| txtInput2.getText() != inputedSequences[1] )
				{
					btnClear.fire();
				}			
				// Reset the stackpanes info so we dont write on top of existing data
				stack.getChildren().clear();
				
				//Create the grid
				grid = new GridPane();
				mainGrid = new Grid(grid);
				setupGrid();
				mainGrid.createGrid(txtInput, txtInput2);

				Label alignmentLabel;
				// Run the selected algorithm
				switch (selectedAlgorithm) {

				// IF LONGEST COMMON SUBSEQUENCE IS SELECTED:
				case LCS:
					// Run an instance of the algorithm
					LCS lcs = new LCS(txtInput.getText(), txtInput2.getText());
					Cell[][] lcsTable = lcs.getScoreTable();
					tracebackCellList = lcs.getTracebackPath();
					
					// For the LCS algorithm we skip row & column zero because its always the same
					for (int i = 0; i < lcsTable.length; i++)
						for (int j = 0; j < lcsTable[i].length; j++)
							mainGrid.addItem(lcsTable[i][j].getScore(), j+1, i+1, tracebackCellList.contains(lcsTable[i][j]));

					// Create the info for the information tab:
					alignmentLabel = new Label("Longest common subsequence:");
					Label commonSubsequence = new Label(lcs.getLCSTraceback());
					//Bottom box info
					bottomSequenceLabel1.setText("Sequence 1: " + lcs.getLCSTraceback());
					bottomSequenceLabel2.setText("Sequence 2:");

					StackPane.setAlignment(alignmentLabel, Pos.TOP_CENTER);
					stack.getChildren().add(alignmentLabel);
					stack.getChildren().add(commonSubsequence);

					break;

				// IF NEEDLEMAN WUNSCH ALGORITHM IS SELECTED:
				case NMW:
					// Run an instance of the algorithm
					SequenceAlignmentWunsch NMW = new SequenceAlignmentWunsch(txtInput.getText(), txtInput2.getText());
					Cell[][] cellTable = NMW.getScoreTable();
					tracebackCellList = NMW.getTracebackPath();
					// For the needleman wunsch algorithm we skip row & column zero because its
					// always the same
					for (int i = 0; i < cellTable.length; i++)
						for (int j = 0; j < cellTable[i].length; j++)
						{
							mainGrid.addItem(cellTable[i][j].getScore(), j+1, i+1, tracebackCellList.contains(cellTable[i][j]));
						}
						
					// Create the info for the information tab:
					alignmentLabel = new Label("Aligned Sequences:");
					Label alignment1 = new Label(NMW.getAlignment()[0] + "\n" + NMW.getAlignment()[1]);
					//Bottom box info
					bottomSequenceLabel1.setText("Sequence 1: " + NMW.getAlignment()[0]);
					bottomSequenceLabel2.setText("Sequence 2: " + NMW.getAlignment()[1]);
					
					StackPane.setAlignment(alignmentLabel, Pos.TOP_CENTER);
					StackPane.setAlignment(alignment1, Pos.CENTER);
					stack.getChildren().add(alignmentLabel);
					stack.getChildren().add(alignment1);

					break;

				// IF SMITH WATERMAN ALGORITHM IS SELECTED:
				case SWM:
					Smith_waterman smith = new Smith_waterman(txtInput.getText(), txtInput2.getText());
					Integer[][] matrix = smith.getMatrix();
					
					for (int i = 1; i < matrix.length+1; i++)
						for (int j = 1; j < matrix[i-1].length+1; j++)
							mainGrid.addItem(matrix[i-1][j-1], j, i,false);
					
					alignmentLabel = new Label("Aligned Sequences:");
					Label alignment2 = new Label(smith.getAlignment()+ "\n" +smith.getAlignment2());
					
					ArrayList<Smith_waterman.Coord> myTrace = smith.getTraceback();
					
					//Smith_waterman.Coord item = myTrace.get(0);
					for(Smith_waterman.Coord item: myTrace)
					{
						System.out.println(item.getX());
						System.out.println(item.getY());
						//mainGrid.addItem(matrix[item.getX()][item.getY()], item.getY()+1, item.getX()+1, true);
						mainGrid.setHighlight(true, item.getY(), item.getX());
					}
					
					StackPane.setAlignment(alignmentLabel, Pos.TOP_CENTER);
					StackPane.setAlignment(alignment2, Pos.CENTER);
					stack.getChildren().add(alignmentLabel);
					stack.getChildren().add(alignment2);

					break;

				default:
					break;
				}
			});

			
			btnNext.setOnAction(e -> {
				
				// If the create button hasn't been pressed, don't run the algorithm
				if (!grid.isVisible())
					return;
				
				if(selectedAlgorithm==Main.algorithmType.LCS || selectedAlgorithm==Main.algorithmType.NMW)
				{
				//Reset the initially highlighted cells if they are all highlighted.
				Cell lastCell = tracebackCellList.get(tracebackCellList.size() - 1);
				if (mainGrid.isHighlighted(lastCell.getCol(), lastCell.getRow()))
				{
					for (int i = 1; i < tracebackCellList.size(); i++)
						mainGrid.setHighlight(false, tracebackCellList.get(i).getCol(), tracebackCellList.get(i).getRow());
					bottomSequenceLabel1.setText("Sequence 1: ");
					bottomSequenceLabel2.setText("Sequence 2: ");
					//Reseting counts as a click so we dont perform the rest of the code.
					return;
				}
				
					//Highlight the next cell in the list
					for (int i = 1; i < tracebackCellList.size(); i++)
					{
						if (mainGrid.isHighlighted(tracebackCellList.get(i).getCol(), tracebackCellList.get(i).getRow()))
							continue;
						else
						{
							//Highlight the next item
							mainGrid.setHighlight(true, tracebackCellList.get(i).getCol(), tracebackCellList.get(i).getRow());

							//Get the aligned sequence up to the current cell.
							switch (selectedAlgorithm) {

							case LCS:
								LCS lcs = new LCS(txtInput.getText(), txtInput2.getText());
								bottomSequenceLabel1.setText("Sequence 1: " + lcs.getLCSTraceback(tracebackCellList.get(i)));
								bottomSequenceLabel2.setText("Sequence 2:");
								break;
							case NMW:
								SequenceAlignmentWunsch NMW = new SequenceAlignmentWunsch(txtInput.getText(), txtInput2.getText());				
								bottomSequenceLabel1.setText("Sequence 1: " + NMW.getAlignment(tracebackCellList.get(i))[0]);
								bottomSequenceLabel2.setText("Sequence 2: " +  NMW.getAlignment(tracebackCellList.get(i))[1]);
								break;
							case SWM:
								

								break;

							default: break;
							}
							//Break out of loop since we highlighted a cell.
							return;
						}

					}

				}
				else
				{
					
					
					//System.out.println("WE MADE IT HERE");
					Smith_waterman smith = new Smith_waterman(txtInput.getText(), txtInput2.getText());
					ArrayList<String> partialAligns= smith.getPartialAligns();
					ArrayList<String> partialAligns2= smith.getPartialAligns2();
					ArrayList<Smith_waterman.Coord> myTrace = smith.getTraceback();
					if(k>partialAligns.size()-1)
						k=0;
					
					
					Smith_waterman.Coord item = myTrace.get(k);

					//reset highlights
					if(k==0)
					{
						for (int i = 1; i < myTrace.size(); i++)
							mainGrid.setHighlight(false, myTrace.get(i).getY(), myTrace.get(i).getX());
						bottomSequenceLabel1.setText("Sequence 1: ");
						bottomSequenceLabel2.setText("Sequence 2: ");
					}
				

								
					//matrix[item.getX()][item.getY()],
					//-100
					//mainGrid.addItem(matrix[item.getX()][item.getY()], item.getY()+1, item.getX()+1, true);
					mainGrid.setHighlight(true, item.getY(), item.getX());
					bottomSequenceLabel1.setText("Sequence 1: " + partialAligns.get(k));
					bottomSequenceLabel2.setText("Sequence 2: " + partialAligns2.get(k));

					k++;
				}

			});

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupGrid() {
		// set up the grid layout: the main grid to be used
		grid.setStyle("-fx-border-style:solid; -fx-border-color:black;");
		grid.gridLinesVisibleProperty().set(true); // DEBUGGING ONLY
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setAlignment(Pos.CENTER);
		grid.setVgap(0);
		grid.setHgap(0);
		grid.setStyle("-fx-border-style:solid; -fx-border-color:black; -fx-background-color: #000000;");

		scroll.setContent(grid);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
