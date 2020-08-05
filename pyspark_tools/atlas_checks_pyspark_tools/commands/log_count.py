import argparse
from atlas_checks_pyspark_tools.commands.base_command import BaseCommand
from pyspark.sql import SparkSession


class LogCount(BaseCommand):
    def __init__(self):
        super().__init__()

        parser = argparse.ArgumentParser()
        parser.add_argument("--input")
        self.args = parser.parse_known_args()[0]

    def run(self):
        spark = SparkSession.builder.appName("LogCount").getOrCreate()
        self.logger.error(spark.read.json(self.args.input, recursiveFileLookup=True).count())
