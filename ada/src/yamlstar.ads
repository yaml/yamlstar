package YAMLStar is
   function Load_JSON (Input : String) return String;
   function Load (Input : String) return String renames Load_JSON;
end YAMLStar;
