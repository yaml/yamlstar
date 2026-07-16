with Ada.Strings.Fixed;
with Ada.Text_IO;
with YAMLStar;

procedure Test_YAMLStar is
   package Fixed renames Ada.Strings.Fixed;

   Fails : Natural := 0;

   procedure Check (Cond : Boolean; Label : String) is
   begin
      if Cond then
         Ada.Text_IO.Put_Line ("ok - " & Label);
      else
         Ada.Text_IO.Put_Line ("not ok - " & Label);
         Fails := Fails + 1;
      end if;
   end Check;

   JSON : constant String := YAMLStar.Load_JSON
     ("test: 42");
begin
   Check (Fixed.Index (JSON, """test"":42") > 0, "load yaml");

   if Fails > 0 then
      raise Program_Error with "Ada YAMLStar tests failed";
   end if;
end Test_YAMLStar;
