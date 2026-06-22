{-# LANGUAGE OverloadedStrings #-}

module Main where

import qualified Data.Aeson.Encode.Pretty as Aeson
import qualified Data.ByteString.Lazy.Char8 as LBS
import qualified Data.Text as T
import System.Environment (getArgs)
import YAMLStar

main :: IO ()
main = do
  args <- getArgs
  case args of
    [] -> putStrLn "Usage: yamlstar-test <yaml-code>"
    (code:_) -> do
      result <- loadYAMLStar (T.pack code)
      LBS.putStrLn $ Aeson.encodePretty result
