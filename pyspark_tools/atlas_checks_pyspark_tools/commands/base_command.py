import logging


class BaseCommand:
    def __init__(self):
        self.logger = logging.getLogger()

    def run(self):
        pass

    @staticmethod
    def get_subclasses():
        command_map = {}
        queue = [BaseCommand]
        while len(queue) > 0:
            parent = queue.pop()
            for command in parent.__subclasses__():
                command_map[command.__name__] = command
                queue.append(command)
        return command_map
