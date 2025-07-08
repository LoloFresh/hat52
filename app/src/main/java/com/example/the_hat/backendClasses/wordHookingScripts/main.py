import pyautogui, pytesseract, time
from PIL import Image
def screenshot(file, x, y, h, w): pyautogui.screenshot(region=(x, y, h, w)).save(file)
def read_test(file): return pytesseract.image_to_string(Image.open(file), lang="rus")
def click(x, y): pyautogui.click(x, y)

fl = "dict_easy.txt"
img = "image.png"
dict = open(fl, 'a', encoding="utf-8")
ct = 0.6
ctall = 5
c1 = 1000
c2 = 5
l = (35, 370, 600, 45)
lb = (220, 550)
le = (220, 490)
r = (1000, 330, 600, 45)
rb = (1190, 520)
re = (1190, 460)
t = -1
for i in range(c1):
    t = (t + 1) % 2
    time.sleep(1.5)
    click(*(lb if t == 0 else rb))
    for j in range(c2):
        time.sleep(ct)
        screenshot(img, *(l if t == 0 else r))
        dict.write(read_test(img))
        click(*(lb if t == 0 else rb))
    screenshot(img, *(l if t == 0 else r))
    dict.write(read_test(img))
    time.sleep(ctall - ct * c2)
    click(*(lb if t == 0 else rb))
    time.sleep(ct)
    click(*(le if t == 0 else re))
