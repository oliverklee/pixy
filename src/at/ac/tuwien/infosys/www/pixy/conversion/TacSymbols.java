package at.ac.tuwien.infosys.www.pixy.conversion;

public class TacSymbols {

	public static final int T_EPSILON = 0;
	public static final int R_statement = 1;
	public static final int R_start = 2;
	public static final int R_top_statement_list = 3;
	public static final int T_dummy = 4;

	private static final String[] yytname = { "epsilon", "statement", "start", "top_statement_list", "dummy" };

	private TacSymbols() {
	}

	static String symToName(int symbol) {
		return yytname[symbol];
	}
}