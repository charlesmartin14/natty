package com.natty.date;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.debug.BlankDebugEventListener;

/**
 * A custom parse event listener to detect the location of date time strings
 */
public class ParseEventListener extends BlankDebugEventListener {
  private int _backtracking = 0;
  private List<Token> _tokens;
  private List<Location> _locations;
  private boolean _inDateTimeAlternative = false;
  private boolean _inDateTime = false;
  
  public ParseEventListener() {
    _tokens = new ArrayList<Token>();
    _locations = new ArrayList<Location>();
  }
  
  public List<Location> getLocations() {
    return _locations;
  }
  
  @Override
  public void enterDecision(int d) {
    _backtracking++;
  }

  @Override
  public void exitDecision(int i) {
    _backtracking--;
  }
  
  @Override
  public void enterRule(String filename, String ruleName) {
    if (_backtracking > 0) return;
    
    if(isDateTime(ruleName)) _inDateTime = true;
    if(isDateTimeAlternative(ruleName)) _inDateTimeAlternative = true;
  }
  
  @Override
  public void exitRule(String filename, String ruleName) {
    if (_backtracking > 0) return;
    
    if(isDateTimeAlternative(ruleName) || isDateTime(ruleName) && !_inDateTimeAlternative) {
      consumeLocation();
    }
    
    if(isDateTime(ruleName)) _inDateTime = false;
    if(isDateTimeAlternative(ruleName)) _inDateTimeAlternative = false;
  }

  @Override
  public void consumeToken(Token token) {
    if (_backtracking > 0) return;
    
    if(_inDateTime || _inDateTimeAlternative) _tokens.add(token);
  }
  
  private boolean isDateTime(final String ruleName) {
    return ruleName.equals("date_time");
  }
  
  private boolean isDateTimeAlternative(final String ruleName) {
    return ruleName.equals("date_time_alternative");
  }
  
  /**
   * Consumes the current token list as a date_time
   */
  private void consumeLocation() {
    if(_tokens.size() == 0) return;
    StringBuilder builder = new StringBuilder();
    for (Token token : _tokens) {
      builder.append(token.getText());
    }
    String text = builder.toString();
    Token startToken = _tokens.get(0);
    int line = startToken.getLine();
    int start = startToken.getCharPositionInLine();
    int end = start + text.length();
    _locations.add(new Location(text, line, start, end));
    _tokens.clear();
  }
}