#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
from atlas_checks_pyspark_tools.commands import *
import logging


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--command")
    return parser.parse_known_args()[0]


def main():
    logger = logging.getLogger()
    logger.info("Test Message - Main Method Has Started")

    try:
        command_map = base_command.BaseCommand.get_subclasses()
        args = get_args()

        if args.command in command_map.keys():
            command_map.get(args.command)().run()
        else:
            logger.error("Unable to find command with name %s, available commands are: %s", args.command,
                         ", ".join(sorted(command_map.keys())))
    except Exception as e:
        logger.error("Python Error: ", e)
        raise e


if __name__ == '__main__':
    main()
