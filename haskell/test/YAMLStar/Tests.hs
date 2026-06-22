{-# LANGUAGE OverloadedStrings #-}

module YAMLStar.Tests where

import qualified Data.Aeson as Aeson
import qualified Data.Vector as V
import Test.Hspec
import YAMLStar

basicTests :: Spec
basicTests = describe "Basic YAML" $ do
  it "parses simple key-value pairs" $ do
    result <- loadYAMLStar "key: value"
    result `shouldBe` Aeson.object [("key", Aeson.String "value")]

  it "parses lists" $ do
    result <- loadYAMLStar "list: [1, 2, 3]"
    result `shouldBe` Aeson.object
      [ ("list", Aeson.Array $ V.fromList
          [Aeson.Number 1, Aeson.Number 2, Aeson.Number 3])
      ]

  it "loads multiple documents" $ do
    result <- loadYAMLStarAll "---\ndoc1\n---\na: 1\n"
    result `shouldBe` Aeson.Array (V.fromList
      [Aeson.String "doc1", Aeson.object [("a", Aeson.Number 1)]])
