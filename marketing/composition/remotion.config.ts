import {Config} from '@remotion/cli/config';
import path from 'node:path';
Config.setPublicDir(path.resolve(__dirname,'..'));
Config.setVideoImageFormat('jpeg');
Config.setOverwriteOutput(true);
