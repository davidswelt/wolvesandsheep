library(ggplot2)

pl <- function(title,da,outfile=NULL)
{
    if (!is.null(outfile))    {png(outfile,width=2000,height=1200)}
    p <- ggplot(data=da, aes(x=time, y=total, col=class)) + geom_line()  + ggtitle(title)
    print(p)
    if (!is.null(outfile))  {dev.off()}
}

d = read.table("log/log.tsv",sep="\t",header=T,quote="")

w <- d[grep(".Wolf$", d$class), ]
s <- d[grep(".Sheep$", d$class), ]
wt <- d[grep(".WolfTeam$", d$class), ]
st <- d[grep(".SheepTeam$", d$class), ]

pl("Wolves", w)
   
pl("Wolves", w, "log/w.png")
pl("Sheep", s,"log/s.png")
pl("Wolf Teams", wt, "log/wt.png")
pl("Sheep Teams", st, "log/st.png")

