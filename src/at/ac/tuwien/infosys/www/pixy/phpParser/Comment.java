package at.ac.tuwien.infosys.www.pixy.phpParser;

public class Comment {
	public int line;
	public int col;
	public String filename;
	public String content;

	public Comment(int _line, int _col, String _filename, String _content) {
		line = _line;
		col = _col;
		filename = _filename;
		content = _content;
	}

}
