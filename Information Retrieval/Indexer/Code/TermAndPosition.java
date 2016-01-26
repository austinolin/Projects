package B;
/**
 * Will store a term and its position in an individual document, to make it easier to manage
 * @author Austin
 *
 */
public class TermAndPosition {

	private Integer position;
    private String termName;
    

    //Takes the term name and the position in the document
    public TermAndPosition(String termName, Integer position) {
        this.termName = termName;
        this.position = position;
    }

    public void setTermName(String termName) {
    	this.termName = termName;
    }
    
    public String getTermName() {
        return this.termName;
    }
    

    public void setPosition(int position) {
    	this.position = position;
    }

    public Integer getPosition() {
        return this.position;
    }

}