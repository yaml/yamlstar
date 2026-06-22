{-# LANGUAGE OverloadedStrings #-}

module Main where

import qualified Data.Aeson as Aeson
import qualified Data.Text as T
import Test.Hspec
import YAMLStar
import YAMLStar.Tests

main :: IO ()
main = hspec $ do
  describe "YAMLStar" $ do
    basicTests

    it "dumps a mapping" $ do
      result <- dumpYAMLStar $ Aeson.object [("key", Aeson.String "value")]
      result `shouldBe` "key: value\n"

    it "reports the library version" $ do
      result <- versionYAMLStar
      T.unpack result `shouldSatisfy` (not . null)
