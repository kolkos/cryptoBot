"""
Test the log method
"""
from inc.General import General

general = General()

kwargs = {
    'class': "Class naam",
    'method': "Method naam",
    'action': "Method called",
    'result': "Test"
}

general.logger(3, **kwargs)

kwargs = {
    'method': "Method naam",
    'action': "Method called",
    'result': "Geen class opgegeven"
}

general.logger(3, **kwargs)

kwargs = {
    'action': "Method called",
    'result': "Geen class en method opgegeven"
}

general.logger(3, **kwargs)