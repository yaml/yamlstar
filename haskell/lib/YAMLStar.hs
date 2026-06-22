{-# LANGUAGE OverloadedStrings #-}

module YAMLStar
  ( YAMLStarError(..)
  , loadYAMLStar
  , loadYAMLStarAll
  , dumpYAMLStar
  , dumpYAMLStarAll
  , versionYAMLStar
  ) where

import Control.Exception (Exception, throwIO)
import Control.Monad.IO.Class (MonadIO, liftIO)
import qualified Data.Aeson as Aeson
import qualified Data.Aeson.Key as Key
import qualified Data.Aeson.KeyMap as KeyMap
import qualified Data.ByteString.Lazy as LBS
import qualified Data.Text as T
import qualified Data.Text.Encoding as TE
import YAMLStar.FFI

data YAMLStarError
  = YAMLStarParseError String
  | YAMLStarRuntimeError String
  | YAMLStarFFIError String
  deriving (Show, Eq)

instance Exception YAMLStarError

loadYAMLStar :: MonadIO m => T.Text -> m Aeson.Value
loadYAMLStar input =
  liftIO $ handleData =<< yamlstarLoadFFI (TE.encodeUtf8 input)

loadYAMLStarAll :: MonadIO m => T.Text -> m Aeson.Value
loadYAMLStarAll input =
  liftIO $ handleData =<< yamlstarLoadAllFFI (TE.encodeUtf8 input)

dumpYAMLStar :: MonadIO m => Aeson.Value -> m T.Text
dumpYAMLStar value =
  liftIO $ handleTextData =<< yamlstarDumpFFI (LBS.toStrict $ Aeson.encode value)

dumpYAMLStarAll :: MonadIO m => Aeson.Value -> m T.Text
dumpYAMLStarAll value =
  liftIO $ handleTextData =<< yamlstarDumpAllFFI (LBS.toStrict $ Aeson.encode value)

versionYAMLStar :: MonadIO m => m T.Text
versionYAMLStar =
  liftIO $ TE.decodeUtf8 <$> yamlstarVersionFFI

handleData :: LBS.ByteString -> IO Aeson.Value
handleData response =
  case Aeson.eitherDecode response of
    Left err -> throwIO $ YAMLStarParseError err
    Right (Aeson.Object obj) ->
      case (KeyMap.lookup (Key.fromString "error") obj, KeyMap.lookup (Key.fromString "data") obj) of
        (Just err, _) -> throwIO $ YAMLStarRuntimeError (show err)
        (_, Just value) -> return value
        _ -> throwIO $ YAMLStarFFIError "Unexpected response from 'libyamlstar'"
    Right _ -> throwIO $ YAMLStarFFIError "Unexpected response from 'libyamlstar'"

handleTextData :: LBS.ByteString -> IO T.Text
handleTextData response = do
  value <- handleData response
  case value of
    Aeson.String text -> return text
    _ -> throwIO $ YAMLStarFFIError "Expected text response from 'libyamlstar'"
