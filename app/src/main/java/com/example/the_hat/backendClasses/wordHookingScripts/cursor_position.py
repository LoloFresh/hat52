import pyautogui, pytesseract, time
from PIL import Image

def cursor_position():
    time.sleep(2)
    return pyautogui.position()

print(*cursor_position())