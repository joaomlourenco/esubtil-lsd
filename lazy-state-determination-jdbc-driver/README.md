# Lazy State Determination JDBC Driver

## Future Improvements
* Check if LSDConnection can do everything it does with just a readMap for futures, 
  and a Queue of instructions to execute
* Separate PreparedStatement instructions from LSDPreparedStatement, i.e. only use LSDPreparedStatement for LSD API 
  operations such as SELECT_LSD
* Implement batch instructions in Statement classes.
* LSDStatement may need extra work when compared to LSDPreparedStatements.
* core.parser should be rewritten to work as an Abstract Syntax Tree.